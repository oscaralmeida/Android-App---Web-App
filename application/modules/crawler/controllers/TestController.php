<?php
/**
 * This controller will process all the jobs
 */
class Crawler_TestController extends Zend_Controller_Action
{
	public function init()
	{
		// we 100% that actions from this controller will be
		// called from CLI so we disabling layout and auto output
		$this->_helper->layout->disableLayout();
		$this->_helper->viewRenderer->setNoRender(true);
	}
	
	public function searchAction()
	{
		$options['search_keywords'] = 'вікна';
		$options['remote_ip'] = '127.0.0.1';
		
		$SearchForm = new Nashmaster_SearchForm($options);
		var_dump($SearchForm);
	}

	public function dataAction()
	{
		$url = 'http://emarket.ua/construction/elevators-escalators';
		
		$job = new Joss_Crawler_Db_Jobs();
		$data = $job->getLastJobByUrl($url);
		
		// load page content
		$rawBody = base64_decode($data['raw_body']);
		
		$emarketUa = new Joss_Crawler_Adapter_Emarketua();
		$emarketUa->loadPage($url, $rawBody);
		
		$links = $emarketUa->getDataLinks();
		var_dump($links);
		
		
	}
	
	public function clearcrawlAction()
	{
		$db = Zend_Db_Table::getDefaultAdapter();

		$sql = 'SELECT *, COUNT(id) items FROM crawl_item GROUP BY adapter_specific_id HAVING COUNT(id) > 1 ORDER BY items DESC';
		$multiItems = $db->fetchAll($sql);
		
		foreach ($multiItems as $multiItem) {
			// search all dublicates
			$sql = 'SELECT * FROM crawl_item WHERE adapter_specific_id = ' . $multiItem['adapter_specific_id'];
			$dublicateItems = $db->fetchAll($sql);
			
			// we should leave only one element and remove all dublicates
			unset($dublicateItems[0]);
			
			$dubIds = array();
			foreach ($dublicateItems as $dublicateItem) {
				$dubIds[] = $dublicateItem['id'];
			}
			$dubIds = implode(',', $dubIds);
			
			$sql1 = 'DELETE FROM crawl_item_contacts WHERE item_id IN (' . $dubIds . ')';
			$sql2 = 'DELETE FROM crawl_item_details WHERE item_id IN (' . $dubIds . ')';
			$sql3 = 'DELETE FROM crawl_item_regions WHERE item_id IN (' . $dubIds . ')';
			$sql4 = 'DELETE FROM crawl_item_services WHERE item_id IN (' . $dubIds . ')';
			
			$db->query($sql1);
			$db->query($sql2);
			$db->query($sql3);
			$db->query($sql4);
			
			// delete items
			$sql = 'DELETE FROM crawl_item WHERE id IN (' . $dubIds . ')';
			$db->query($sql);
		}
	}
	
	
	public function seocitiesAction()
	{
		$Cities = new Searchdata_Model_Cities();
		
		$ukrainianCities = $Cities->getItems();
		
		foreach ($ukrainianCities as $city)
		{
			// create russian SEO URL
			$name = (!empty($city->uniq_name)) ? $city->uniq_name : $city->name;
			$city->seo_name = $this->normalizeUrl($name);
			
			$name_uk = (!empty($city->uniq_name_uk)) ? $city->uniq_name_uk : $city->name_uk;
			$city->seo_name_uk = $this->normalizeUrl($name_uk);
			
			$city->save();
		}
	}
	
	public function seoservicesAction()
	{
		$Services = new Searchdata_Model_Services();
		$serList = $Services->getAllItems();
		
		foreach ($serList as $service)
		{
			$service->seo_name = $this->normalizeUrl($service->name);
			$service->seo_name_uk = $this->normalizeUrl($service->name_uk);
			$service->save();
		}
		
	}
	
	public function normalizeUrl($text)
	{
		$text = mb_strtolower($text);
		
		$text = mb_ereg_replace('(,|\.|-)', ' ', $text);
		$text = mb_ereg_replace(' +', ' ', $text);
		$text = trim($text);
		$text = mb_ereg_replace(' ', '-', $text);
		
		return $text;
	}
	
	
	public function citiesAction()
	{
		$Cities = new Searchdata_Model_Cities();
		$CitiesDistances = new Searchdata_Model_CitiesDistances();
		
		$ukrainianCities = $Cities->getItems();
		
		foreach ($ukrainianCities as $city) {
			$relCities = $Cities->getRelatedCities($city->latitude, $city->longitude);
			foreach ($relCities as $relCity) {
				$relRecord = $CitiesDistances->fetchNew();
				$relRecord->parent_city_id = $city->city_id;
				$relRecord->city_id = $relCity['city_id'];
				$relRecord->distance = $relCity['distance'];
				$relRecord->is_region_center = $relCity['is_region_center'];
				$relRecord->name = $relCity['name'];
				$relRecord->name_uk = $relCity['name_uk'];
				$relRecord->seo_name = $relCity['seo_name'];
				$relRecord->seo_name_uk = $relCity['seo_name_uk'];
				$relRecord->save();
				unset($relRecord);
			}
		}
	}
}