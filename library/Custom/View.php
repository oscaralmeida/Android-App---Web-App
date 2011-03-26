<?php

class Custom_View extends Zend_View
{
	private $_auth = null;
	private $_translate = null;
	private $_locale = null;
	private $_request = null;
	
	private $_subnavigation = null;
	private $_breadcrumbs = null;
	
	public function __construct($options)
	{
		$this->_auth = Zend_Auth::getInstance();
		parent::__construct($options);
	}
	
	/**
	 * Next two methods will add the sub-navigation functionality that will allow to add navigation URLs
	 * to the line under the main navigation
	 *
	 * sub-navigation controlls can be controlled from the controller action template
	 */
	public function addSubNavication($url, $title, $cssClass = null)
	{
		$this->_subnavigation[] = '<a href="' . $url . '"'
			. (($cssClass !== null) ? ' class="' . $cssClass . '"' : ''  ) . '>'
			. $title . '</a>';
		
	}
	
	public function renderSubNavigation()
	{
		if (count($this->_subnavigation) < 1) {
			return;
		}
		
		echo '<div id="navigation-bar">' . implode("\n", $this->_subnavigation) . '</div>';
	}
	
	/**
	 * Next two methods are designed to create and render breadcrumbs
	 * 
	 * it will accept the array as 
	 * array(
	 * 	   'title' => 'url',
	 * 	   'title' => 'url',
	 * 	   'title' => 'url',
	 * )
	 * @param array $breadcrumbsArray
	 */
	public function setBreadcrumbs($breadcrumbsArray)
	{
		foreach ($breadcrumbsArray as  $crumbTitle => $url) {
			if (empty($url)) {
				$this->_breadcrumbs[] = '<span>' . $crumbTitle . '</span>';
			} else {
				$this->_breadcrumbs[] = '<a href="' . $url . '" class="crumb"'
				. '>' . $crumbTitle . '</a>';
			}
		}

	}
	
	public function renderBreadcrumbs()
	{
		if (count($this->_breadcrumbs) < 2) {
			return;
		}
		
		echo '<div id="breadcrumbs">' . implode("\n > ", $this->_breadcrumbs) . '</div>';
	}
	
	/**
	 * Returns text representation of current locale
	 */
	public function getLocale()
	{
		return $this->_locale->toString();
	}
	
	public function setTranslation($translation)
	{
		$this->_translate = $translation;
		$this->_locale = Zend_Registry::get('Zend_Locale');
	}
	
	public function profileLink()
    {
        if ($this->_auth->hasIdentity()) {
            $userData = $this->_auth->getStorage()->read();
            return 'Profile: <a href="/profile/' . $userData['username'] . '">' . $userData['real_name'] .  '</a> <a href="' . $this->url(
			array(
				'lang' => $this->getLocale(),
				'controller' => 'login',
				'action' => 'logout',
				'module' => 'default',
			)
			,'default') . '">' . $this->T('logout') . '</a>';
        }
        
        return '<a ' . ( ($this->getModuleName() == 'default' && $this->getControllerName() == 'login') ? 'class="active"' : '' )  . ' href="' . $this->url(
			array(
				'lang' => $this->getLocale(),
				'controller' => 'login',
				'action' => 'index',
				'module' => 'default',
			)
			,'default') . '">' . $this->T('login') . '</a>';
		
    }

    /*
    public function isAdmin()
    {
    	$userData = $this->_auth->getStorage()->read();
    	if ($userData['roles'] == 'admin') {
    		return true;
    	}
    	
    	return false;
    }
    */
    
    public function getLanguages()
    {
    	$langs = $this->_translate->getList();

        $locale = $this->_translate->getLocale();
    	
    	$languagesTitles = array();
	    foreach($langs as $language => $content) {
	    	$languagesTitles[$language] = Zend_Locale::getTranslation($language, 'language', $language);
	    }

    	return $languagesTitles;
    }
    
    public function getRequest()
    {
    	if (empty($this->_request)) {
    		$this->_request = Zend_Controller_Front::getInstance()->getRequest();
    	}
    	
    	return $this->_request;
    }
    
    public function getActionName()
    {
    	return $this->getRequest()->getActionName();
    }
    
    public function getControllerName()
    {
    	return $this->getRequest()->getControllerName();
    }
    
    public function getModuleName()
    {
    	return $this->getRequest()->getModuleName();
    }
    
    public function getImagesUrl()
    {
    	return '/images/'; // we will move this to CDN in the future
    }
    
    public function T($messageid = null)
    {
    	/**
    	 * Process the arguments
    	 */
        $options = func_get_args();
        
        array_shift($options);
 
        $count  = count($options);
        $locale = null;
        if ($count > 0) {
            if (Zend_Locale::isLocale($options[($count - 1)], null, false) !== false) {
                $locale = array_pop($options);
            }
        }
 
        if ((count($options) === 1) and (is_array($options[0]) === true)) {
            $options = $options[0];
        }
        
    	/**
         * Proxify the call to Zend_Translate_Adapter
         */
        $message = $this->_translate->translate($messageid, $locale);
        
        /**
         * If no any options provided then just return message
         */
        if ($count === 0) {
            return $message;
        }
 
        /**
         * Apply options in case we have them
         */
        return vsprintf($message, $options);
    }

    /**
     * Outputs language options with the ability to select desired one
     */
    public function languageSelector()
    {
    	$langs = $this->getLanguages();
    	
    	$output = '';
    	foreach ($langs as $key => $lang) {
    		
    		if ($this->getLocale() == $key) {
    			$output .= ' <span class="active-language">' . $lang . '</span> ';
    		} else {
    			// $output .= ' <a href="' . $this->url(array('lang' => $key), null, null, $key) . '">' . $lang . '</a> ';
    			$output .= ' <a href="/' . $key . '/">' . $lang . '</a> ';
    		}

    	}

    	return $output;
    }
    
   /**
    * output form for users feedback
    */
    public function feedbackForm()
    {
    	return $this->render("feedbackform.phtml");
    }

	/**
	 * json_encode() replacement that handles cyrillyc characters correctly
	 *
	 * Thanks to: http://www.php.net/manual/en/function.json-encode.php#78719
	 *
	 * @param mixed $a
	 * @return string json code
	 */
	public function php2js($a=false)
	{
		  if (is_null($a)) return 'null';
		  if ($a === false) return 'false';
		  if ($a === true) return 'true';
		  if (is_scalar($a))
		  {
		    if (is_float($a))
		    {
		      // Always use "." for floats.
		      $a = str_replace(",", ".", strval($a));
		    }
		
		    // All scalars are converted to strings to avoid indeterminism.
		    // PHP's "1" and 1 are equal for all PHP operators, but
		    // JS's "1" and 1 are not. So if we pass "1" or 1 from the PHP backend,
		    // we should get the same result in the JS frontend (string).
		    // Character replacements for JSON.
		    static $jsonReplaces = array(array("\\", "/", "\n", "\t", "\r", "\b", "\f", '"'),
		    array('\\\\', '\\/', '\\n', '\\t', '\\r', '\\b', '\\f', '\"'));
		    return '"' . str_replace($jsonReplaces[0], $jsonReplaces[1], $a) . '"';
		  }
		  $isList = true;
		  for ($i = 0, reset($a); $i < count($a); $i++, next($a))
		  {
		    if (key($a) !== $i)
		    {
		      $isList = false;
		      break;
		    }
		  }
		  $result = array();
		  if ($isList)
		  {
		    foreach ($a as $v) $result[] = $this->php2js($v);
		    return '[ ' . join(', ', $result) . ' ]';
		  }
		  else
		  {
		    foreach ($a as $k => $v) $result[] = $this->php2js($k).': '.$this->php2js($v);
		    return '{ ' . join(', ', $result) . ' }';
		  }
	}
	
	/**
	 * Cut the text the the specified amount lenght
	 *
	 * @param string $string
	 * @param int $cutoff
	 * @return string
	 */
	public function cuttext($string, $cutoff)
	{
	    if (mb_strlen($string) < $cutoff) {
	        return $string;
	    } else {
	        // look for a space
	        $new_str = mb_substr($string, 0, $cutoff);
	        return mb_strrchr($new_str, ' ', true) . '...';
	    }
	}
}