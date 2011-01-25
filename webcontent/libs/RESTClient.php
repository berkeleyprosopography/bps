<?php

require_once "HTTP/Request2.php";

class RESTClient {

	private $root_url = "";
	private $curr_url = "";
	private $user_name = "";
	private $password = "";
	private $response = "";
	private $responseBody = "";
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
			$this->req->setMethod(HTTP_REQUEST_METHOD_GET);
			break;
		case "POST":
			$this->req->setMethod(HTTP_REQUEST_METHOD_POST);
			if ($arr != null) {
				$this->req->addPostParameter($arr);
			}
			break;
		case "PUT":
			$this->req->setMethod(HTTP_REQUEST_METHOD_PUT);
			// to-do
			break;
		case "DELETE":
			$this->req->setMethod(HTTP_REQUEST_METHOD_DELETE);
			// to-do
			break;
		}
	}

	public function sendRequest() {

		//FIXME need to handler errors more gracefully.
		try {
			$this->response = $this->req->send();
			if (200 == $this->response->getStatus()) {
				$this->responseBody = $this->response->getBody();
			} else {
				echo 'Unexpected HTTP status: ' . $response->getStatus() . ' ' .
					$response->getReasonPhrase();
				die();
			}
		} catch (HTTP_Request2_Exception $e) {
			echo 'Error: ' . $e->getMessage();
			die();
		}
	}

	public function getResponse() {
		return $this->responseBody;
	}

}
?>

