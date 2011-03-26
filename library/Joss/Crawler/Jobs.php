<?php
/**
 * Triget crawling jobs by starting appropriate
 * jobs quelle with initial URLs from each crawling adapter
 *
 * @version		0.0.1
 * @package		joss-crawler
 * @see			http://webdevbyjoss.blogspot.com/
 * @author		Joseph Chereshnovsky <joseph.chereshnovsky@gmail.com>
 * @copyright	2010
 * @license		GPL
 */
class Joss_Crawler_Jobs
{
	/**
	 * The list of supported creawler adapters
	 *
	 * @var array
	 */
	protected $_adapters = array(
		'Joss_Crawler_Adapter_Emarketua'
	);
	
	/**
	 * The instance of Joss_Crawler_Db_Jobs for internal use
	 *
	 * @var Joss_Crawler_Db_Jobs
	 */
	protected $_dbJobs = null;
	
	/**
	 * Lets initialize the table gateway
	 *
	 * TODO: change this to dependency injection
	 */
	public function __construct()
	{
		$this->_dbJobs = new Joss_Crawler_Db_Jobs();
	}
	
	/**
	 * Check the quelle
	 *
	 * TODO: Check if prewious quele was finished and only then start new quelle
	 */
	public function startQuelle()
	{
		if (!$this->_dbJobs->isFinished()) {
			return false;
		}
		
		foreach ( $this->_adapters as $adapterClass ) {
			$Adapter = new $adapterClass();
			$urls = $Adapter->getInitialUrl();
			foreach ($urls as $url) {
				$this->_dbJobs->createJob($url);
			}
		}
	}
	
	/**
	 * Process each next job from the queue
	 */
	public function processNextJob()
	{
		// get next job from database
		$job = $this->_dbJobs->getJobForProcessing();

		if (null == $job) {
			/**
			 * TODO: write appropriate message to the output and to the log
			 */
			return false;
		}

		$this->processData($job['url'], $job['raw_body']);
		$this->_dbJobs->finishJob($job['crawl_jobs_id']);
		return true;
	}

	/**
	 * Recognizes data and stores it into database
	 *
	 * @param string $url
	 * @param string $raw_body
	 * @return null
	 */
	public function processData($url, $raw_body)
	{
		// recognize the adapter & extract content
		$Adapter = $this->getLoadedAdapter($url, $raw_body);
		if (null === $Adapter) {
			throw new Exception('Unable to load adapter for: ' . $url);
		}

		//grap the URLs with interesting  data and create new jobs for that pages
		$links = $Adapter->getDataLinks();

		if (!empty($links)) {
			foreach ($links as $key => $link) {
				$this->_dbJobs->createJob($link['url']);
			}
		}
		
		// grap the data from the page
		// late return in case no data were recognized on this page
		$data = $Adapter->getData();
		if (null === $data) {
			return ;
		}
		
		// store grabed inforamtion into crawler database
		$Items = new Joss_Crawler_Db_Items();
		
		foreach ($data as $advert) {
			$Items->add($advert);
		}
	}
	
	/**
	 * Returns the last job by URL
	 *
	 * in job server we potentically can have couple same URL with the different status
	 * in case current URL were already processed and rgiht now we need to update information
	 * we have by downloading the newer content
	 *
	 * anyway we should optimize this part to have only one unique URL in jobs database
	 *
	 * @param string $url
	 * @return Joss_Crawler_Adapter_Abstract
	 */
	public function getLastJobByUrl($url)
	{
		// get next job from database
		$DbJobs = new Joss_Crawler_Db_Jobs();
		return $DbJobs->getLastJobByUrl($url);
	}

	/**
	 * Creates the adapter and loads page content inside
	 *
	 * @param string $url
	 * @param string $rawBody
	 * @return Joss_Crawler_Adapter_Abstract
	 */
	public function getLoadedAdapter($url, $rawBody)
	{
		// recognize the adapter
		$Adapter = $this->getAdapterByUrl($url);
		if (null === $Adapter) {
			throw new Exception ('No adapter can be loaded for URL:' . $url);
		}
		
		// load page content
		$rawBody = base64_decode($rawBody);
		$Adapter->loadPage($url, $rawBody);
		
		return $Adapter;
	}
	
	/**
	 * It recognizes the adapter by provided URL
	 * and returns apropriate adapter instance
	 *
	 * TODO: this is not very oprimal, we should cache recognized adapters somwhow to
	 *       avoid re-recognision of simmilar URLs from the same domain/adapter and make
	 *       everything fater in overal operation
	 *
	 * @param string $url
	 * @return Joss_Crawler_Adapter_Abstract
	 */
	public function getAdapterByUrl($url)
	{
		foreach ($this->_adapters as $adapterClass) {
			$Adapter = new $adapterClass();
			if ($Adapter->matchDataLink($url)) {
				return $Adapter;
			}
			unset($Adapter);
		}
	}

}