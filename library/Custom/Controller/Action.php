<?php

class Custom_Controller_Action extends Zend_Controller_Action
{
	public function preDispatch()
	{
		$auth = Zend_Auth::getInstance();
		$acl = new Custom_Acl();
        
		if ($auth->hasIdentity()) {
            $userInfo = $auth->getStorage()->read();
        } else {
        	$userInfo['roles'] = Custom_Acl::ROLE_GUEST;
        }
        
        // Lets check the permissions
        $request = $this->getRequest();
        $resource = 'mvc:' . ($request->module ? $request->module : 'default');
        
        if (!$acl->isAllowed($userInfo['roles'], $resource)) {

        	$request
        		->setParam('resource', $resource)
        		->setParam('role', $userInfo['roles'])
        		->setModuleName('default')
	            ->setControllerName('error') // using the errorController seems appropriate
	            ->setActionName('denied')
	            ->setDispatched(false);
	            ;
	        return;
        }
        
	}
}