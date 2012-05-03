<?php

/***************************************************************************************
 *                                                                                     *
 * This file is part of the XPertMailer package (http://xpertmailer.sourceforge.net/)  *
 *                                                                                     *
 * XPertMailer is free software; you can redistribute it and/or modify it under the    *
 * terms of the GNU General Public License as published by the Free Software           *
 * Foundation; either version 2 of the License, or (at your option) any later version. *
 *                                                                                     *
 * XPertMailer is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A     *
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.           *
 *                                                                                     *
 * You should have received a copy of the GNU General Public License along with        *
 * XPertMailer; if not, write to the Free Software Foundation, Inc., 51 Franklin St,   *
 * Fifth Floor, Boston, MA  02110-1301  USA                                            *
 *                                                                                     *
 * XPertMailer SMTP & POP3 PHP Mail Client. Can send and read messages in MIME Format. *
 * Copyright (C) 2007  Tanase Laurentiu Iulian                                         *
 *                                                                                     *
 ***************************************************************************************/

if (!class_exists('XPM3_SMTP')) require_once 'XPM3_SMTP.php';

class XPM3_MAIL {

	protected $_conn = false;
	protected $_from = false;
	protected $_host = false;
	protected $_path = false;
	protected $_text = false;
	protected $_html = false;
	protected $_priority = false;

	protected $_port;
	protected $_timeout;

	protected $_to = array();
	protected $_cc = array();
	protected $_bcc = array();
	protected $_header = array();
	protected $_attach = array();

	protected $_vord = array('local');
	protected $_delv = array('local' => '', 'client' => '', 'relay' => '');
	protected $_unique = 0;

	public $result = array();

	public function __construct() {
		$this->_port = XPM3_SMTP::PORT;
		$this->_timeout = XPM3_SMTP::TIMEOUT;
	}

	public function delivery($str = null) {
		try {
			if (is_string($str)) {
				$str = strtolower($str);
				$arr = array();
				$set = true;
				foreach (explode('-', $str) as $val) {
					if (isset($this->_delv[$val])) $arr[] = $val;
					else {
						$set = false;
						break;
					}
				}
				if ($set) {
					$this->_vord = $arr;
					return true;
				} else throw new Exception('invalid argument value', 0);
			} else throw new Exception('invalid argument type', 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e); }
	}

	private function _addaddr($addr, $name, $charset, $encoding, &$rep, $exception) {
		$ret = false;
		try {
			$errors = array();
			if (!is_string($addr)) $errors[] = 'invalid address type';
			if ($name == null) $name = '';
			else if (!is_string($name)) $errors[] = 'invalid name type';
			if ($charset == null) $charset = XPM3_MIME::HCHARSET;
			else if (!is_string($charset)) $errors[] = 'invalid charset type';
			if ($encoding == null) $encoding = XPM3_MIME::HENCODING;
			else if (!is_string($encoding)) $errors[] = 'invalid encoding type';
			if (count($errors) == 0) {
				if (isset($rep[$addr])) throw XPM3_FUNC::exception_rewrite($exception, 'address already exists', 1);
				else if (!XPM3_FUNC::is_mail($addr)) throw XPM3_FUNC::exception_rewrite($exception, 'invalid address value', 1);
				else {
					$charset = XPM3_FUNC::str_clear($charset, array(' '));
					$charlen = strlen($charset);
					if ($charlen < 4 || $charlen > 64) {
						$errors[] = 'invalid charset value';
						$charset = XPM3_MIME::HCHARSET;
					}
					$encoding = XPM3_FUNC::str_clear($encoding, array(' '));
					$encoding = strtolower($encoding);
					if ($encoding == '' || !isset(XPM3_MIME::$_hencoding[$encoding])) {
						$errors[] = 'invalid encoding value';
						$encoding = XPM3_MIME::HENCODING;
					}
					$name = XPM3_FUNC::str_clear($name);
					$name = trim($name);
					if ($name == '') $rep[$addr] = false;
					else {
						$code = XPM3_MIME::encode_header($name, $charset, $encoding);
						$rep[$addr] = ($code != $name) ? $code : '"'.str_replace('"', '\\"', $name).'"';
					}
					if (count($errors) > 0) {
						$ret = true;
						throw XPM3_FUNC::exception_rewrite($exception, implode(', ', $errors), 1);
					}
					return true;
				}
			} else throw XPM3_FUNC::exception_rewrite($exception, implode(', ', $errors), 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, $ret); }
	}

	public function addto($addr = null, $name = null, $charset = null, $encoding = null) {
		return $this->_addaddr($addr, $name, $charset, $encoding, $this->_to, new XPM3_Exception());
	}

	public function addcc($addr = null, $name = null, $charset = null, $encoding = null) {
		return $this->_addaddr($addr, $name, $charset, $encoding, $this->_cc, new XPM3_Exception());
	}

	public function addbcc($addr = null) {
		try {
			if (!is_string($addr)) throw new Exception('invalid address type', 0);
			else if (!XPM3_FUNC::is_mail($addr)) throw new Exception('invalid address value', 0);
			else if (isset($this->_bcc[$addr])) throw new Exception('address already exists', 1);
			else {
				$this->_bcc[$addr] = false;
				return true;
			}
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	private function _deladdr($addr, &$rep, $exception) {
		try {
			if ($addr == null) {
				$rep = array();
				return true;
			} else if (is_string($addr) && XPM3_FUNC::is_mail($addr)) {
				$found = false;
				$new = array();
				foreach ($rep as $key => $val) {
					if ($key == $addr) $found = true;
					else $new[$key] = $val;
				}
				if ($found) {
					$rep = $new;
					return true;
				} else return false;
			} else throw XPM3_FUNC::exception_rewrite($exception, 'invalid address value', 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e); }
	}

	public function delto($addr = null) {
		return $this->_deladdr($addr, $this->_to, new XPM3_Exception());
	}

	public function delcc($addr = null) {
		return $this->_deladdr($addr, $this->_cc, new XPM3_Exception());
	}

	public function delbcc($addr = null) {
		return $this->_deladdr($addr, $this->_bcc, new XPM3_Exception());
	}

	public function from($addr = null, $name = null, $charset = null, $encoding = null) {
		$ret = $this->_from = false;
		try {
			$errors = array();
			if (!is_string($addr)) $errors[] = 'invalid address type';
			else if (!XPM3_FUNC::is_mail($addr)) $errors[] = 'invalid address value';
			if ($name == null) $name = '';
			else if (!is_string($name)) $errors[] = 'invalid name type';
			if ($charset == null) $charset = XPM3_MIME::HCHARSET;
			else if (!is_string($charset)) $errors[] = 'invalid charset type';
			if ($encoding == null) $encoding = XPM3_MIME::HENCODING;
			else if (!is_string($encoding)) $errors[] = 'invalid encoding type';
			if (count($errors) == 0) {
				$charset = XPM3_FUNC::str_clear($charset, array(' '));
				$charlen = strlen($charset);
				if ($charlen < 4 || $charlen > 64) {
					$errors[] = 'invalid charset value';
					$charset = XPM3_MIME::HCHARSET;
				}
				$encoding = XPM3_FUNC::str_clear($encoding, array(' '));
				$encoding = strtolower($encoding);
				if ($encoding == '' || !isset(XPM3_MIME::$_hencoding[$encoding])) {
					$errors[] = 'invalid encoding value';
					$encoding = XPM3_MIME::HENCODING;
				}
				$name = XPM3_FUNC::str_clear($name);
				$name = trim($name);
				if ($name == '') $this->_from = array('address' => $addr, 'name' => false);
				else {
					$code = XPM3_MIME::encode_header($name, $charset, $encoding);
					$repl = ($code != $name) ? $code : '"'.str_replace('"', '\\"', $name).'"';
					$this->_from = array('address' => $addr, 'name' => $repl);
				}
				if (count($errors) > 0) {
					$ret = true;
					throw new Exception(implode(', ', $errors), 1);
				}
				return true;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, $ret); }
	}

	public function addheader($name = null, $value = null, $charset = null, $encoding = null) {
		$ret = false;
		try {
			$errors = array();
			if (!is_string($name)) $errors[] = 'invalid name type';
			if (!is_string($value)) $errors[] = 'invalid value type';
			if ($charset == null) $charset = XPM3_MIME::HCHARSET;
			else if (!is_string($charset)) $errors[] = 'invalid charset type';
			if ($encoding == null) $encoding = XPM3_MIME::HENCODING;
			else if (!is_string($encoding)) $errors[] = 'invalid encoding type';
			if (count($errors) == 0) {
				$name = XPM3_FUNC::str_clear($name);
				$name = trim($name);
				$value = XPM3_FUNC::str_clear($value);
				$value = trim($value);
				if ($name == '') $errors[] = 'invalid name value';
				if ($value == '') $errors[] = 'value are empty';
				if (count($errors) == 0) {
					$ver = strtolower($name);
					$err = false;
					if ($ver == 'to') $err = 'can not set "To", for this, use function "AddTo()"';
					else if ($ver == 'cc') $err = 'can not set "Cc", for this, use function "AddCc()"';
					else if ($ver == 'bcc') $err = 'can not set "Bcc", for this, use function "AddBcc()"';
					else if ($ver == 'from') $err = 'can not set "From", for this, use function "From()"';
					else if ($ver == 'subject') $err = 'can not set "Subject", for this, use function "Send()"';
					else if ($ver == 'x-priority') $err = 'can not set "X-Priority", for this, use function "Priority()"';
					else if ($ver == 'x-msmail-priority') $err = 'can not set "X-MSMail-Priority", for this, use function "Priority()"';
					else if ($ver == 'date') $err = 'can not set "Date", this value is automaticaly set';
					else if ($ver == 'content-type') $err = 'can not set "Content-Type", this value is automaticaly set';
					else if ($ver == 'content-transfer-encoding') $err = 'can not set "Content-Transfer-Encoding", this value is automaticaly set';
					else if ($ver == 'content-disposition') $err = 'can not set "Content-Disposition", this value is automaticaly set';
					else if ($ver == 'mime-version') $err = 'can not set "Mime-Version", this value is automaticaly set';
					else if ($ver == 'x-mailer') $err = 'can not set "X-Mailer", this value is automaticaly set';
					if ($err) throw new Exception($err, 0);
					else {
						$charset = XPM3_FUNC::str_clear($charset, array(' '));
						$charlen = strlen($charset);
						if ($charlen < 4 || $charlen > 64) {
							$errors[] = 'invalid charset value';
							$charset = XPM3_MIME::HCHARSET;
						}
						$encoding = XPM3_FUNC::str_clear($encoding, array(' '));
						$encoding = strtolower($encoding);
						if ($encoding == '' || !isset(XPM3_MIME::$_hencoding[$encoding])) {
							$errors[] = 'invalid encoding value';
							$encoding = XPM3_MIME::HENCODING;
						}
						$this->_header[] = array('name' => ucfirst($name), 'value' => XPM3_MIME::encode_header($value, $charset, $encoding));
						if (count($errors) > 0) {
							$ret = true;
							throw new Exception(implode(', ', $errors), 1);
						}
						return true;
					}
				} else throw new Exception(implode(', ', $errors), 0);
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, $ret); }
	}

	public function delheader($name = null) {
		try {
			if ($name == null) {
				$this->_header = array();
				return true;
			} else if(is_string($name)) {
				$name = XPM3_FUNC::str_clear($name);
				$name = trim(strtolower($name));
				if ($name != '') {
					$found = false;
					$new = array();
					foreach ($this->_header as $arr) {
						if (strtolower($arr['name']) == $name) $found = true;
						else $new[] = $arr;
					}
					if ($found) {
						$this->_header = $new;
						return true;
					}
				} else return false;
			} else throw new Exception('invalid name value', 1);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function host($name = null, &$mx) {
		try {
			if ($name == null) {
				$this->_host = false;
				return true;
			} else if (is_string($name)) {
				$name = XPM3_FUNC::str_clear($name);
				$name = trim(strtolower($name));
				if ($name != '' && ($name == 'localhost' || XPM3_FUNC::is_hostname($name))) {
					$mx = false;
					$this->_host = $name;
					if ($name != 'localhost') $mx = XPM3_FUNC::is_win() ? XPM3_FUNC::getmxrr_win($name, $mxarr) : getmxrr($name, $mxarr);
					return true;
				} else throw new Exception('invalid hostname value', 0);
			} else throw new Exception('invalid hostname type', 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function returnpath($addr = null) {
		try {
			if ($addr == null) {
				$this->_path = false;
				return true;
			} else if (is_string($addr)) {
				if (trim($addr) != '' && XPM3_FUNC::is_mail($addr)) {
					$this->_path = $addr;
					return true;
				} else throw new Exception('invalid address value', 0);
			} else throw new Exception('invalid address type', 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function port($value = null) {
		try {
			if (is_int($value) && $value > 0) {
				$this->_port = $value;
				return true;
			} else {
				$this->_port = XPM3_SMTP::PORT;
				throw new Exception('invalid value type', 0);
			}
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function timeout($value = null) {
		try {
			if (is_int($value) && $value > 0) {
				$this->_timeout = $value;
				return true;
			} else {
				$this->_timeout = XPM3_SMTP::TIMEOUT;
				throw new Exception('invalid value type', 0);
			}
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function priority($level = null) {
		try {
			if ($level == null) {
				$this->_priority = false;
			} else if (is_int($level) && ($level == 1 || $level == 3 || $level == 5)) {
				if ($level == 1) $this->_priority = array('1', 'High');
				else if ($level == 3) $this->_priority = array('3', 'Normal');
				else if ($level == 5) $this->_priority = array('5', 'Low');
			} else if (is_string($level)) {
				$level = XPM3_FUNC::str_clear($level, array(' '));
				$level = strtolower($level);
				if ($level == 'high') $this->_priority = array('1', 'High');
				else if ($level == 'normal') $this->_priority = array('3', 'Normal');
				else if ($level == 'low') $this->_priority = array('5', 'Low');
				else throw new Exception('invalid level value', 0);
			} else throw new Exception('invalid level type', 0);
			return true;
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function text($value = null, $charset = null, $encoding = null, $disposition = null) {
		if ($charset == null) $charset = XPM3_MIME::MCHARSET;
		if ($encoding == null) $encoding = XPM3_MIME::MENCODING;
		if ($disposition == null) $disposition = 'inline';
		if ($this->_text = XPM3_MIME::message($value, 'text/plain', null, $charset, $encoding, $disposition, null, new XPM3_Exception())) return true;
		else return false;
	}

	public function html($value = null, $charset = null, $encoding = null, $disposition = null) {
		if ($charset == null) $charset = XPM3_MIME::MCHARSET;
		if ($encoding == null) $encoding = XPM3_MIME::MENCODING;
		if ($disposition == null) $disposition = 'inline';
		if ($this->_html = XPM3_MIME::message($value, 'text/html', null, $charset, $encoding, $disposition, null, new XPM3_Exception())) return true;
		else return false;
	}

	public function attachsource($value = null, $type = null, $name = null, $charset = null, $encoding = null, $disposition = null, $id = null) {
		if ($encoding == null) $encoding = 'base64';
		if ($disposition == null) $disposition = 'attachment';
		if ($arr = XPM3_MIME::message($value, $type, $name, $charset, $encoding, $disposition, $id, new XPM3_Exception())) {
			$this->_attach[] = $arr;
			return true;
		} else return false;
	}

	public function attachfile($file = null, $type = null, $name = null, $charset = null, $encoding = null, $disposition = null, $id = null) {
		try {
			$error = '';
			if (!is_string($file)) $error = 'invalid filename type';
			else {
				$file = XPM3_FUNC::str_clear($file);
				$file = trim($file);
				if ($file != '' && is_file($file) && is_readable($file)) {
					if ($type == null) $type = XPM3_FUNC::mimetype($file);
					if ($name == null) {
						$exp1 = explode('/', $file);
						$name = $exp1[count($exp1)-1];
						$exp2 = explode('\\', $name);
						$name = $exp2[count($exp2)-1];
					}
					if ($encoding == null) $encoding = 'base64';
					if ($disposition == null) $disposition = 'attachment';
				} else $error = 'invalid file resource';
			}
			if ($error == '') {
				if ($arr = XPM3_MIME::message(file_get_contents($file), $type, $name, $charset, $encoding, $disposition, $id, new XPM3_Exception())) {
					$this->_attach[] = $arr;
					return true;
				} else return false;
			} else throw new Exception($error, 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function delattach($name = null) {
		try {
			if ($name == null) {
				$this->_attach = array();
				return true;
			} else if (is_string($name)) {
				$name = XPM3_FUNC::str_clear($name);
				$name = trim(strtolower($name));
				if ($name != '') {
					$found = false;
					$new = array();
					foreach ($this->_attach as $arr) {
						if (strtolower($arr['name']) == $name) $found = true;
						else $new[] = $arr;
					}
					if ($found) {
						$this->_attach = $new;
						return true;
					} else return false;
				} else throw new Exception('invalid name value', 1);
			} else throw new Exception('invalid name type', 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, false); }
	}

	public function relay($host = null, $user = null, $pass = null, $port = null, $ssl = null, $timeout = null) {
		$name = $this->_host ? $this->_host : null;
		$arr = XPM3_SMTP::connect($host, $user, $pass, $port, $ssl, $timeout, $name, new XPM3_Exception());
		$this->result = $arr['result'];
		if ($this->_conn = $arr['connection']) return true;
		else return false;
	}

	public function send($subject = null, $charset = null, $encoding = null) {
		$ret = false;
		try {
			if ($charset == null) $charset = XPM3_MIME::MCHARSET;
			if ($encoding == null) $encoding = XPM3_MIME::MENCODING;
			$errors = array();
			if (!is_string($subject)) $errors[] = 'invalid subject type';
			else {
				$subject = XPM3_FUNC::str_clear($subject);
				$subject = trim($subject);
				if ($subject == '') $errors[] = 'invalid subject value';
			}
			if (!is_string($charset)) $errors[] = 'invalid charset type';
			if (!is_string($encoding)) $errors[] = 'invalid encoding type';
			if (count($this->_to) == 0) $errors[] = 'to address is not set';
			if (!($this->_text || $this->_html)) $errors[] = 'message is not set';
			if (count($errors) == 0) {
				$subject = XPM3_MIME::encode_header($subject, $charset, $encoding);
				$charset = XPM3_FUNC::str_clear($charset, array(' '));
				$charlen = strlen($charset);
				if ($charlen < 4 || $charlen > 22) {
					$errors[] = 'invalid charset value';
					$charset = XPM3_MIME::HCHARSET;
				}
				$encoding = XPM3_FUNC::str_clear($encoding, array(' '));
				$encoding = strtolower($encoding);
				if ($encoding == '' || !isset(XPM3_MIME::$_hencoding[$encoding])) {
					$errors[] = 'invalid encoding value';
					$encoding = XPM3_MIME::HENCODING;
				}
				$hlocal = $hclient = $this->_header;
				if ($this->_from) $hfrom = $this->_from['name'] ? $this->_from['name'].' <'.$this->_from['address'].'>' : $this->_from['address'];
				else {
					$hfrom = @ini_get('sendmail_from');
					if ($hfrom == '' || !XPM3_FUNC::is_mail($hfrom)) $hfrom = (isset($_SERVER['SERVER_ADMIN']) && XPM3_FUNC::is_mail($_SERVER['SERVER_ADMIN'])) ? $_SERVER['SERVER_ADMIN'] : 'postmaster@localhost';
				}
				$ato = $alladdrs = array();
				foreach ($this->_to as $kto => $vto) {
					$ato[] = $vto ? $vto.' <'.$kto.'>' : $kto;
					$alladdrs[] = $kto;
				}
				$hto = implode(', '.XPM3_MIME::CRLF."\t", $ato);
				$hcc = $hbcc = false;
				if (count($this->_cc) > 0) {
					$acc = array();
					foreach ($this->_cc as $kcc => $vcc) {
						$acc[] = $vcc ? $vcc.' <'.$kcc.'>' : $kcc;
						$alladdrs[] = $kcc;
					}
					$hcc = implode(', '.XPM3_MIME::CRLF."\t", $acc);
				}
				if (count($this->_bcc) > 0) {
					$abcc = array();
					foreach ($this->_bcc as $kbcc => $vbcc) {
						$abcc[] = $kbcc;
						$alladdrs[] = $kbcc;
					}
					$hbcc = implode(', '.XPM3_MIME::CRLF."\t", $abcc);
				}
				$hxmail = array('name' => base64_decode('WC1NYWlsZXI='), 'value' => base64_decode('WFBNMyB2LjAuNCA8d3d3LnhwZXJ0bWFpbGVyLmNvbT4='));
				$hlocal[] = array('name' => 'From', 'value' => $hfrom);
				if ($hcc) $hlocal[] = array('name' => 'Cc', 'value' => $hcc);
				if ($hbcc) $hlocal[] = array('name' => 'Bcc', 'value' => $hbcc);
				$hlocal[] = $hxmail;
				$hclient[] = array('name' => 'From', 'value' => $hfrom);
				$hclient[] = array('name' => 'To', 'value' => $hto);
				$hclient[] = array('name' => 'Subject', 'value' => $subject);
				if ($hcc) $hclient[] = array('name' => 'Cc', 'value' => $hcc);
				$hclient[] = array('name' => 'Date', 'value' => date('D, d M Y H:i:s O \(T\)'));
				if ($this->_priority && is_array($this->_priority)) {
					$hlocal[] = array('name' => 'X-Priority', 'value' => $this->_priority[0]);
					$hlocal[] = array('name' => 'X-MSMail-Priority', 'value' => $this->_priority[1]);
					$hclient[] = array('name' => 'X-Priority', 'value' => $this->_priority[0]);
					$hclient[] = array('name' => 'X-MSMail-Priority', 'value' => $this->_priority[1]);
				}
				$hclient[] = $hxmail;
				$hclient[] = array('name' => 'Message-Id', 'value' => '<'.XPM3_MIME::unique($this->_unique++).'@xpertmailer.com>');
				$message = XPM3_MIME::compose($this->_text, $this->_html, $this->_attach, $this->_unique);
				$this->_unique += 3;
				$header['local'] = $header['client'] = '';
				foreach ($hlocal as $arrloc) $header['local'] .= $arrloc['name'].': '.$arrloc['value'].XPM3_MIME::CRLF;
				foreach ($hclient as $arrcli) $header['client'] .= $arrcli['name'].': '.$arrcli['value'].XPM3_MIME::CRLF;
				$header['local'] .= $message['addheader'];
				$header['client'] .= $message['addheader'];
				$name = $this->_host ? $this->_host : '';
				$from = $this->_from ? $this->_from['address'] : $hfrom;
				$path = $this->_path ? $this->_path : '';
				foreach ($this->_vord as $delivery) {
					if (!$ret) {
						if ($delivery == 'relay') {
							if ($this->_conn && is_resource($this->_conn)) {
								$res = XPM3_SMTP::sendtohost($this->_conn, $alladdrs, $from, $header['client'].XPM3_MIME::CRLF.XPM3_MIME::CRLF.$message['body'], $name, $path, $this->_port, $this->_timeout, new XPM3_Exception());
								$ret = $res[0];
								$this->result = $res[1];
							} else {
								$ret = false;
								$errors[] = 'relay connection is not set or invalid ';
								break;
							}
						} else if ($delivery == 'client') {
							$ret = true;
							foreach ($alladdrs as $maddr) {
								$exp = explode('@', $maddr);
								$res = XPM3_SMTP::sendtohost($exp[1], array($maddr), $from, $header['client'].XPM3_MIME::CRLF.XPM3_MIME::CRLF.$message['body'], $name, $path, $this->_port, $this->_timeout, new XPM3_Exception());
								if (!$res[0]) $ret = false;
								$this->result[$maddr] = $res[1];
							}
						} else if ($delivery == 'local') {
							$replh = XPM3_FUNC::is_win() ? $header['local'] : str_replace("\r\n", "\n", $header['local']);
							$replb = XPM3_FUNC::is_win() ? str_replace("\n.", "\n..", $message['body']) : str_replace("\r\n", "\n", $message['body']);
							$rpath = (!XPM3_FUNC::is_win() && $this->_path) ? '-f'.$this->_path : null;
							$spath = $this->_path ? @ini_set('sendmail_from', $this->_path) : false;
							if (!mail(implode(', ', $ato), $subject, $replb, $replh, $rpath)) {
								$res = XPM3_SMTP::sendtohost('127.0.0.1', $alladdrs, $from, $header['client'].XPM3_MIME::CRLF.XPM3_MIME::CRLF.$message['body'], $name, $path, XPM3_SMTP::PORT, $this->_timeout, new XPM3_Exception());
								$ret = $res[0];
								$this->result = array('local' => array('error' => 'mail() return FALSE'), '127.0.0.1' => $res[1]);
							} else {
								$this->result['success'] = 'mail() return TRUE';
								$ret = true;
							}
							if ($spath) @ini_restore('sendmail_from');
						}
					} else break;
				}
				if (count($errors) > 0) throw new Exception(implode(', ', $errors), 1);
				return $ret;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return XPM3_FUNC::exception_handler($e, $ret); }
	}

	public function quit() {
		$res = XPM3_SMTP::quit($this->_conn);
		if ($res[1]) $this->result = $res[1];
		return $res[0];
	}

	public function close() {
		return XPM3_FUNC::close($this->_conn);
	}

}

?>