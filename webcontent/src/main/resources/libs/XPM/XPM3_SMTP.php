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

if (!class_exists('XPM3_MIME')) require_once 'XPM3_MIME.php';

class XPM3_SMTP extends XPM3_MIME {

	const PORT = 25;
	const TIMEOUT = 30;
	const COM_TIMEOUT = 5;

	static protected $_cssl = array('tls' => '', 'ssl' => '', 'sslv2' => '', 'sslv3' => '');

	static public function quit($fp) {
		$ret = $res = false;
		if ($fp && is_resource($fp)) {
			if (fwrite($fp, 'QUIT'.self::CRLF)) {
				if ($vget = @fgets($fp, 1024)) $res['success'] = $vget;
				$ret = true;
			} else $res[27] = 'can not write';
			@fclose($fp);
		} else $res[26] = 'invalid resource connection';
		return array($ret, $res);
	}

	static public function connect($host = null, $user = null, $pass = null, $port = null, $ssl = null, $timeout = null, $name = null, $exception = null) {
		try {
			$errors = array();
			if ($host == null) $host = '127.0.0.1';
			else if (!is_string($host)) $errors[] = 'invalid hostname type';
			if ($user == null) $user = '';
			else if (!is_string($user)) $errors[] = 'invalid username type';
			if ($pass == null) $pass = '';
			else if (!is_string($pass)) $errors[] = 'invalid password type';
			if ($port == null) $port = self::PORT;
			else if (!(is_int($port) && $port > 0)) $errors[] = 'invalid port type/value';
			if ($ssl == null) $ssl = false;
			else if (is_string($ssl) && isset(self::$_cssl[strtolower($ssl)])) $ssl = strtolower($ssl);
			else if (!is_bool($ssl)) $errors[] = 'invalid ssl value';
			if ($timeout == null) $timeout = self::TIMEOUT;
			else if (!(is_int($timeout) && $timeout > 0)) $errors[] = 'invalid timeout type/value';
			if ($name == null) $name = '';
			else if (!is_string($name)) $errors[] = 'invalid name type';
			if (count($errors) == 0) {
				$host = self::str_clear($host, array(' '));
				$host = strtolower($host);
				if ($host == '') $errors[] = 'invalid hostname value';
				else if (self::is_ipv4($host)) $iphost = $host;
				else {
					$iphost = gethostbyname($host);
					if ($iphost == $host) $errors[] = 'invalid hostname address';
				}
				$user = self::str_clear($user);
				$pass = self::str_clear($pass);
				if (($user != '' && $pass == '') || ($user == '' && $pass != '')) $errors[] = 'invalid username and password value combination';
				if (count($errors) == 0) {
					$arr['result'] = $response = array();
					$arr['connection'] = false;
					if (is_bool($ssl)) $ver = $ssl ? 'ssl' : 'tcp';
					else $ver = $ssl;
					$name = self::str_clear($name, array(' '));
					if ($name == '') $name = '127.0.0.1';
					if (!$fp = stream_socket_client($ver.'://'.$iphost.':'.$port, $errno, $errstr, $timeout)) $arr['result'][0] = $errstr;
					else if (!stream_set_timeout($fp, self::COM_TIMEOUT)) $arr['result'][1] = 'could not set stream timeout';
					else if (!self::result($fp, $response, 220)) $arr['result'][2] = $response;
					else if (!fwrite($fp, 'EHLO '.$name.self::CRLF)) $arr['result'][3] = 'can not write';
					else {
						$continue = false;
						if (!self::result($fp, $response, 250)) {
							if (!fwrite($fp, 'HELO '.$name.self::CRLF)) $arr['result'][4] = 'can not write';
							else if (!self::result($fp, $response, 250)) $arr['result'][5] = $response;
							else $continue = true;
						} else $continue = true;
						if ($continue) {
							if ($user != '') {
								if (!fwrite($fp, 'AUTH LOGIN'.self::CRLF)) $arr['result'][6] = 'can not write';
								else if (!self::result($fp, $response, 334)) {
									if (!fwrite($fp, 'AUTH PLAIN '.base64_encode($user.chr(0).$user.chr(0).$pass).self::CRLF)) $arr['result'][7] = 'can not write';
									else if (!self::result($fp, $response, 235)) $arr['result'][8] = $response;
									else $arr['connection'] = $fp;
								} else if (!fwrite($fp, base64_encode($user).self::CRLF)) $arr['result'][9] = 'can not write';
								else if (!self::result($fp, $response, 334)) $arr['result'][10] = $response;
								else if (!fwrite($fp, base64_encode($pass).self::CRLF)) $arr['result'][11] = 'can not write';
								else if (!self::result($fp, $response, 235)) $arr['result'][12] = $response;
								else $arr['connection'] = $fp;
							} else $arr['connection'] = $fp;
						}
					}
					if (!$arr['connection']) self::close($fp);
					else $arr['result']['success'] = $response;
					return $arr;
				} else throw self::exception_rewrite($exception, implode(', ', $errors), 0);
			} else throw self::exception_rewrite($exception, implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function sendtohost($host, $addrs, $from, $message, $name = '', $path = '', $port = self::PORT, $timeout = self::TIMEOUT, $exception = null) {
		try {
			$errors = array();
			if (!(is_string($host) || is_resource($host))) $errors[] = 'invalid ip/connection type';
			if (!is_array($addrs)) $errors[] = 'invalid mail address destination type';
			else {
				if (count($addrs) > 0) {
					foreach ($addrs as $dest) {
						if (!(is_string($dest) && self::is_mail($dest))) {
							$errors[] = 'invalid mail address destination type/value';
							break;
						}
					}
				} else $errors[] = 'invalid mail address destination type';
			}
			if (!is_string($from)) $errors[] = 'invalid mail from address type';
			if (!is_string($path)) $errors[] = 'invalid path type';
			if (!is_string($message)) $errors[] = 'invalid message type';
			if (!is_string($name)) $errors[] = 'invalid name type';
			if (!(is_int($port) && $port > 0)) $errors[] = 'invalid port type';
			if (!(is_int($timeout) && $timeout > 0)) $errors[] = 'invalid timeout type';
			if (count($errors) == 0) {
				$res = array();
				$ret = false;
				$fp = false;
				$quit = true;
				if (!is_resource($host)) {
					$host = self::str_clear($host, array(' '));
					$host = strtolower($host);
					$iparr = array();
					if (self::is_ipv4($host)) $iparr[$host] = $host;
					else {
						$havemx = self::is_win() ? self::getmxrr_win($host, $mx) : getmxrr($host, $mx);
						if ($havemx) {
							foreach ($mx as $mxhost) {
								$iphost = gethostbyname($mxhost);
								if ($iphost != $mxhost && !isset($iparr[$iphost]) && self::is_ipv4($iphost)) $iparr[$iphost] = $iphost;
							}
						} else {
							$iphost = gethostbyname($host);
							if ($iphost != $host && self::is_ipv4($iphost)) $iparr[$iphost] = $iphost;
						}
					}
					if (count($iparr) > 0) {
						foreach ($iparr as $ipaddr) {
							$conn = self::connect($ipaddr, '', '', $port, false, $timeout, $name, $exception);
							if ($fp = $conn['connection']) break;
							else $res = $conn['result'];
						}
					} else $res['error'] = 'can not find any valid ip address';
				} else {
					$quit = false;
					$fp = $host;
				}
				if ($fp) {
					if (!fwrite($fp, 'MAIL FROM:<'.(($path == '') ? $from : $path).'>'.self::CRLF)) $res[13] = 'can not write';
					else if (!self::result($fp, $response, 250)) $res[14] = $response;
					else {
						$continue = true;
						foreach ($addrs as $dest) {
							if (!fwrite($fp, 'RCPT TO:<'.$dest.'>'.self::CRLF)) {
								$res[15] = 'can not write';
								$continue = false;
								break;
							} else if (!self::result($fp, $response, 250, 251)) {
								$res[16] = $response;
								$continue = false;
								break;
							}
						}
						if ($continue) {
							if (!fwrite($fp, 'DATA'.self::CRLF)) $res[17] = 'can not write';
							else if (!self::result($fp, $response, 354)) $res[18] = $response;
							else {
								foreach (explode(self::CRLF, $message) as $line) {
									if ($line == '.') $line = '..';
									if (!fwrite($fp, $line.self::CRLF)) {
										$res[19] = 'can not write';
										$continue = false;
										break;
									}
								}
								if ($continue) {
									if (!fwrite($fp, '.'.self::CRLF)) $res[20] = 'can not write';
									else if (!self::result($fp, $response, 250)) $res[21] = $response;
									else {
										if (!fwrite($fp, 'RSET'.self::CRLF)) $res[22] = 'can not write';
										else if (!self::result($fp, $response, 250)) $res[23] = $response;
										else if ($quit) {
											if (!fwrite($fp, 'QUIT'.self::CRLF)) $res[24] = 'can not write';
											else if (!$vget = @fgets($fp, 1024)) $res[25] = $response;
											else $res['success'] = $vget;
										} else $res['success'] = $response;
										$ret = true;
									}
								}
							}
						}
					}
				}
				return array($ret, $res);
			} else throw self::exception_rewrite($exception, implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

}

?>