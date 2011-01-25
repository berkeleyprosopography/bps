<?php

require_once "HTTP/Request2.php";

class RESTClient {

	private $root_url = "";
	private $curr_url = "";
	private $user_name = "";
	private $password = "";
	private $response = "";
	private $responseBody = "";
	private $errorBody = "";
	private $req = null;

	public function __construct($root_url = "", $user_name = "", $password = "") {
		$this->root_url = $this->curr_url = $root_url;
		$this->user_name = $user_name;
		$this->password = $password;
		if ($root_url != "") {
			$this->createRequest("GET");
			$this->sendRequest();
		}
		return true;
	}

	public function createRequest($url, $method, $arr = null) {
		$this->curr_url = $url;
		$this->req =& new HTTP_Request2($url);
		if ($this->user_name != "" && $this->password != "") {
			$this->req->setAuth($this->user_name, $this->password);
		}        

		switch($method) {
		case "GET":
			$this->req->setMethod(HTTP_Request2::METHOD_GET);
			break;
		case "POST":
			$this->req->setMethod(HTTP_Request2::METHOD_POST);
			if ($arr != null) {
				$this->req->addPostParameter($arr);
			}
			break;
		case "PUT":
			$this->req->setMethod(HTTP_Request2::METHOD_PUT);
			// to-do
			break;
		case "DELETE":
			$this->req->setMethod(HTTP_Request2::METHOD_DELETE);
			// to-do
			break;
		}
	}

	public function setJSONMode() {
		$this->setHeader('Accept', 'application/json');
	}

	public function setHeader($header, $value) {
		if($this->req != null) {
			$this->req->setHeader($header, $value);
		}
	}
	
	public function sendRequest() {

		try {
			$this->response = $this->req->send();
			if (200 == $this->response->getStatus()) {
				$this->responseBody = $this->response->getBody();
			} else {
				$this->errorBody = 'Unexpected HTTP status: ' . $this->response->getStatus() . ' ' .
					$this->response->getReasonPhrase();
				return false;
			}
		} catch (HTTP_Request2_Exception $e) {
			$this->errorBody = 'Error: ' . $e->getMessage();
			return false;
		}
		return true;
	}

	public function getResponse() {
		return $this->responseBody;
	}

	public function getError() {
		return $this->errorBody;
	}

}

?>
