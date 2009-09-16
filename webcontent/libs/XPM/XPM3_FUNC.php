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

class XPM3_Exception extends Exception {
   public $message = '';
   public $code = 0;
   public function __construct() {
       parent::__construct($this->message, $this->code);
   }
}

class XPM3_FUNC {

	static public $_extensions = array(
		'z'    => 'application/x-compress', 
		'xls'  => 'application/x-excel', 
		'gtar' => 'application/x-gtar', 
		'gz'   => 'application/x-gzip', 
		'cgi'  => 'application/x-httpd-cgi', 
		'php'  => 'application/x-httpd-php', 
		'js'   => 'application/x-javascript', 
		'swf'  => 'application/x-shockwave-flash', 
		'tar'  => 'application/x-tar', 
		'tgz'  => 'application/x-tar', 
		'tcl'  => 'application/x-tcl', 
		'src'  => 'application/x-wais-source', 
		'zip'  => 'application/zip', 
		'kar'  => 'audio/midi', 
		'mid'  => 'audio/midi', 
		'midi' => 'audio/midi', 
		'mp2'  => 'audio/mpeg', 
		'mp3'  => 'audio/mpeg', 
		'mpga' => 'audio/mpeg', 
		'ram'  => 'audio/x-pn-realaudio', 
		'rm'   => 'audio/x-pn-realaudio', 
		'rpm'  => 'audio/x-pn-realaudio-plugin', 
		'wav'  => 'audio/x-wav', 
		'bmp'  => 'image/bmp', 
		'fif'  => 'image/fif', 
		'gif'  => 'image/gif', 
		'ief'  => 'image/ief', 
		'jpe'  => 'image/jpeg', 
		'jpeg' => 'image/jpeg', 
		'jpg'  => 'image/jpeg', 
		'png'  => 'image/png', 
		'tif'  => 'image/tiff', 
		'tiff' => 'image/tiff', 
		'css'  => 'text/css', 
		'htm'  => 'text/html', 
		'html' => 'text/html', 
		'txt'  => 'text/plain', 
		'rtx'  => 'text/richtext', 
		'vcf'  => 'text/x-vcard', 
		'xml'  => 'text/xml', 
		'xsl'  => 'text/xsl', 
		'mpe'  => 'video/mpeg', 
		'mpeg' => 'video/mpeg', 
		'mpg'  => 'video/mpeg', 
		'mov'  => 'video/quicktime', 
		'qt'   => 'video/quicktime', 
		'asf'  => 'video/x-ms-asf', 
		'asx'  => 'video/x-ms-asf', 
		'avi'  => 'video/x-msvideo', 
		'vrml' => 'x-world/x-vrml', 
		'wrl'  => 'x-world/x-vrml');

	static public function exception_handler($exception, $ret = null) {
		$arrs = $exception->getTrace();
		$code = $exception->getCode();
		if ($code == 0) $mess = 'Error';
		else if ($code == 1) $mess = 'Warning';
		else $mess = 'Notice';
		$emsg = '<b>'.$mess.'</b>: '.$exception->getMessage().
			' on '.$arrs[0]['class'].$arrs[0]['type'].$arrs[0]['function'].
			' in <b>'.$arrs[0]['file'].'</b> on line <b>'.$arrs[0]['line'].'</b><br />'."\n";
		if ($code == 0) die($emsg);
		else echo $emsg;
		return $ret;
	}

	static public function exception_rewrite($exception, $message, $code) {
		if ($exception == null) $exception = new Exception($message, $code);
		else {
			$exception->message = $message;
			$exception->code = $code;
		}
		return $exception;
	}

	static public function result($conn, &$resp, $code1, $code2 = null, $code3 = null) {
		$resp = array();
		$ret = true;
		if ($conn && is_resource($conn)) {
			do {
				if ($result = fgets($conn, 1024)) {
					$resp[] = $result;
					$rescode = substr($result, 0, 3);
					if (!($rescode == $code1 || $rescode == $code2 || $rescode == $code3)) {
						$ret = false;
						break;
					}
				} else {
					$resp[] = 'can not read';
					$ret = false;
					break;
				}
			} while ($result[3] == '-');
		} else {
			$resp[] = 'invalid resource connection';
			$ret = false;
		}
		return $ret;
	}

	static public function is_win() {
		return (strtoupper(substr(PHP_OS, 0, 3)) === 'WIN');
	}

	static public function close($conn) {
		return ($conn && is_resource($conn)) ? fclose($conn) : false;
	}

	static public function str_clear($str, $addrep = array()) {
		try {
			$errors = array();
			$rep = array("\r", "\n", "\t");
			if (is_array($addrep)) {
				if (count($addrep) > 0) {
					foreach ($addrep as $strrep) {
						if (is_string($strrep) && $strrep != '') $rep[] = $strrep;
						else {
							$errors[] = 'invalid array value';
							break;
						}
					}
				}
			} else $errors[] = 'invalid array type';
			if (!is_string($str)) $errors[] = 'invalid argument type';
			if (count($errors) == 0) return ($str == '') ? '' : str_replace($rep, '', $str);
			else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function is_alpha($strval, $numeric = true, $addstr = '') {
		try {
			$errors = array();
			if (!is_string($strval)) $errors[] = 'invalid value type';
			if (!is_bool($numeric)) $errors[] = 'invalid numeric type';
			if (!is_string($addstr)) $errors[] = 'invalid additional type';
			if (count($errors) == 0) {
				if ($strval != '') {
					$lists = 'abcdefghijklmnoqprstuvwxyzABCDEFGHIJKLMNOQPRSTUVWXYZ'.$addstr;
					if ($numeric) $lists .= '1234567890';
					$len1 = strlen($strval);
					$len2 = strlen($lists);
					$match = true;
					for ($i = 0; $i < $len1; $i++) {
						$found = false;
						for ($j = 0; $j < $len2; $j++) {
							if ($lists{$j} == $strval{$i}) {
								$found = true;
								break;
							}
						}
						if (!$found) {
							$match = false;
							break;
						}
					}
					return $match;
				} else return false;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function is_hostname($str, $addr = false) {
		try {
			$errors = array();
			if (!is_string($str)) $errors[] = 'invalid value type';
			if (!is_bool($addr)) $errors[] = 'invalid address type';
			if (count($errors) == 0) {
				$ret = false;
				if (trim($str) != '' && self::is_alpha($str, true, '-.')) {
					if (count($exphost1 = explode('.', $str)) > 1 && !(strstr($str, '.-') || strstr($str, '-.'))) {
						$set1 = $set2 = true;
						foreach ($exphost1 as $expstr1) {
							if ($expstr1 == '') {
								$set1 = false;
								break;
							}
						}
						foreach (($exphost2 = explode('-', $str)) as $expstr2) {
							if ($expstr2 == '') {
								$set2 = false;
								break;
							}
						}
						$ext = $exphost1[count($exphost1)-1];
						$len = strlen($ext);
						if ($set1 && $set2 && $len > 1 && $len < 7 && self::is_alpha($ext, false)) $ret = true;
					}
				}
				return ($ret && $addr && gethostbyname($str) == $str) ? false : $ret;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function is_ipv4($str) {
		try {
			if (is_string($str)) return (trim($str) != '' && ip2long($str) && count(explode('.', $str)) === 4);
			else throw new Exception('invalid argument value', 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function getmxrr_win($hostname, &$mxhosts) {
		$mxhosts = array();
		try {
			if (is_string($hostname)) {
				if (self::is_hostname($hostname)) {
					$hostname = strtolower($hostname);
					$retstr = exec('nslookup -type=mx '.$hostname, $retarr);
					if ($retstr && count($retarr) > 0) {
						foreach ($retarr as $line) {
							if (preg_match('/.*mail exchanger = (.*)/', $line, $matches)) $mxhosts[] = $matches[1];
						}
					}
				}
				return (count($mxhosts) > 0);
			} else throw new Exception('invalid argument type', 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function is_mail($addr, $vermx = false) {
		try {
			$errors = array();
			if (!is_string($addr)) $errors[] = 'invalid address type';
			if (!is_bool($vermx)) $errors[] = 'invalid MX type';
			if (count($errors) == 0) {
				$ret = (count($exp = explode('@', $addr)) === 2 && $exp[0] != '' && $exp[1] != '' && self::is_alpha($exp[0], true, '_-.') && (self::is_hostname($exp[1]) || self::is_ipv4($exp[1])));
				if ($ret && $vermx) {
					if (self::is_ipv4($exp[1])) $ret = false;
					else $ret = self::is_win() ? self::getmxrr_win($exp[1], $mxh) : getmxrr($exp[1], $mxh);
				}
				return $ret;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function mimetype($filename) {
		try {
			$ret = 'application/octet-stream';
			if (is_string($filename)) {
				$filename = self::str_clear($filename);
				$filename = trim($filename);
				if ($filename != '') {
					if (count($exp = explode('.', $filename)) >= 2) {
						$ext = strtolower($exp[count($exp)-1]);
						if (isset(self::$_extensions[$ext])) $ret = self::$_extensions[$ext];
					}
					return $ret;
				} else throw new Exception('invalid argument value', 0);
			} else throw new Exception('invalid argument type', 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

}

?>