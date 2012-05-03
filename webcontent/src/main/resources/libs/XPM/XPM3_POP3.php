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

class XPM3_POP3 extends XPM3_MIME {

	const PORT = 110;
	const TIMEOUT = 30;
	const COM_TIMEOUT = 5;

	static protected $_cssl = array('tls' => '', 'ssl' => '', 'sslv2' => '', 'sslv3' => '');

	static public function disconnect($conn) {
		XPM3_FUNC::close($conn);
		return false;
	}

	static public function connect($host = null, $user = null, $pass = null, $port = null, $ssl = null, $timeout = null) {
		$fp = false;
		try {
			$errors = array();
			if (!is_string($host)) $errors[] = 'invalid hostname type';
			if (!is_string($user)) $errors[] = 'invalid username type';
			if (!is_string($pass)) $errors[] = 'invalid password type';
			if ($port == null) $port = self::PORT;
			else if (!(is_int($port) && $port > 0)) $errors[] = 'invalid port type/value';
			if ($ssl == null) $ssl = false;
			else if (is_string($ssl) && isset(self::$_cssl[strtolower($ssl)])) $ssl = strtolower($ssl);
			else if (!is_bool($ssl)) $errors[] = 'invalid ssl value';
			if ($timeout == null) $timeout = self::TIMEOUT;
			else if (!(is_int($timeout) && $timeout > 0)) $errors[] = 'invalid timeout type/value';
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
				if ($user == '') $errors[] = 'invalid username value';
				if ($pass == '') $errors[] = 'invalid password value';
				if (count($errors) == 0) {
					if (is_bool($ssl)) $ver = $ssl ? 'ssl' : 'tcp';
					else $ver = $ssl;
					if (!$fp = stream_socket_client($ver.'://'.$iphost.':'.$port, $errno, $errstr, $timeout)) throw new Exception('0 -> '.$errstr, 1);
					else if (!stream_set_timeout($fp, self::COM_TIMEOUT)) throw new Exception('1 -> could not set stream timeout', 1);
					else if (!self::result($fp, $response, '+OK')) throw new Exception('2 -> '.implode(', ', $response), 1);
					else if (!fwrite($fp, 'USER '.$user.self::CRLF)) throw new Exception('3 -> can not write', 1);
					else if (!self::result($fp, $response, '+OK')) throw new Exception('4 -> '.implode(', ', $response), 1);
					else if (!fwrite($fp, 'PASS '.$pass.self::CRLF)) throw new Exception('5 -> can not write', 1);
					else if (!self::result($fp, $response, '+OK')) throw new Exception('6 -> '.implode(', ', $response), 1);
					else return $fp;
				} else throw new Exception(implode(', ', $errors), 0);
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e, self::disconnect($fp)); }
	}

	static public function pstat($conn = null) {
		try {
			if (!($conn && is_resource($conn))) throw new Exception('invalid resource connection', 0);
			else if (!fwrite($conn, 'STAT'.self::CRLF)) throw new Exception('0 -> can not write', 1);
			else if (!self::result($conn, $response, '+OK')) throw new Exception('1 -> '.implode(', ', $response), 1);
			else {
				if (count($exp = explode(' ', substr($response[0], 4, -strlen(self::CRLF)))) == 2) {
					$val1 = intval($exp[0]);
					$val2 = intval($exp[1]);
					if (strval($val1) === $exp[0] && strval($val2) === $exp[1]) return array($val1, $val2);
					else throw new Exception('3 -> '.implode(', ', $response), 1);
				} else throw new Exception('2 -> '.implode(', ', $response), 1);
			}
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function plist($conn = null, $msg = null) {
		try {
			$errors = array();
			if (!($conn && is_resource($conn))) $errors[] = 'invalid resource connection';
			if ($msg == null) $msg = 0;
			else if (!(is_int($msg) && $msg >= 0)) $errors[] = 'invalid message number';
			if (count($errors) == 0) {
				$num = ($msg > 0) ? true : false;
				if (!fwrite($conn, 'LIST'.($num ? ' '.$msg : '').self::CRLF)) throw new Exception('0 -> can not write', 1);
				else if (!self::result($conn, $response, '+OK')) throw new Exception('1 -> '.implode(', ', $response), 1);
				else {
					$arr = array();
					if ($num) {
						if (count($exp = explode(' ', substr($response[0], 4, -strlen(self::CRLF)))) == 2) {
							$val1 = intval($exp[0]);
							$val2 = intval($exp[1]);
							if (strval($val1) === $exp[0] && strval($val2) === $exp[1]) $arr[$val1] = $val2;
							else throw new Exception('3 -> '.implode(', ', $response), 1);
						} else throw new Exception('2 -> '.implode(', ', $response), 1);
					} else {
						$err = false;
						do {
							if ($res = fgets($conn, 1024)) {
								if (count($exp = explode(' ', substr($res, 0, -strlen(self::CRLF)))) == 2) {
									$val1 = intval($exp[0]);
									$val2 = intval($exp[1]);
									if (strval($val1) === $exp[0] && strval($val2) === $exp[1]) $arr[$val1] = $val2;
								} else if ($res[0] != '.') throw new Exception('5 -> '.$res, 1);
							} else throw new Exception('4 -> can not read', 1);
						} while ($res[0] != '.');
						if ($err) throw new Exception($err, 1);
					}
					return (count($arr) > 0) ? $arr : false;
				}
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function pretr($conn = null, $msg = null) {
		try {
			$errors = array();
			if (!($conn && is_resource($conn))) $errors[] = 'invalid resource connection';
			if (!(is_int($msg) && $msg > 0)) $errors[] = 'invalid message type';
			if (count($errors) == 0) {
				if (!fwrite($conn, 'RETR '.$msg.self::CRLF)) throw new Exception('0 -> can not write', 1);
				else if (!self::result($conn, $response, '+OK')) throw new Exception('1 -> '.implode(', ', $response), 1);
				else {
					$ret = '';
					do {
						if ($res = fgets($conn, 1024)) $ret .= $res;
						else throw new Exception('2 -> can not read', 1);
					} while ($res != '.'.self::CRLF);
					return substr($ret, 0, -strlen(self::CRLF.'.'.self::CRLF));
				}
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function pdele($conn = null, $msg = null) {
		try {
			$errors = array();
			if (!($conn && is_resource($conn))) $errors[] = 'invalid resource connection';
			if (!(is_int($msg) && $msg > 0)) $errors[] = 'invalid message number';
			if (count($errors) == 0) {
				if (!fwrite($conn, 'DELE '.$msg.self::CRLF)) throw new Exception('0 -> can not write', 1);
				else if (!self::result($conn, $response, '+OK')) throw new Exception('1 -> '.implode(', ', $response), 1);
				else return true;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function pnoop($conn = null) {
		try {
			if (!($conn && is_resource($conn))) throw new Exception('invalid resource connection', 0);
			else if (!fwrite($conn, 'NOOP'.self::CRLF)) throw new Exception('0 -> can not write', 1);
			else if (!self::result($conn, $response, '+OK')) throw new Exception('1 -> '.implode(', ', $response), 1);
			else return true;
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function prset($conn = null) {
		try {
			if (!($conn && is_resource($conn))) throw new Exception('invalid resource connection', 0);
			else if (!fwrite($conn, 'RSET'.self::CRLF)) throw new Exception('0 -> can not write', 1);
			else if (!self::result($conn, $response, '+OK')) throw new Exception('1 -> '.implode(', ', $response), 1);
			else return true;
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function pquit($conn = null) {
		try {
			if (!($conn && is_resource($conn))) throw new Exception('invalid resource connection', 0);
			else if (!fwrite($conn, 'QUIT'.self::CRLF)) throw new Exception('0 -> can not write', 1);
			else if (!self::result($conn, $response, '+OK')) throw new Exception('1 -> '.implode(', ', $response), 1);
			else {
				XPM3_FUNC::close($conn);
				return true;
			}
		} catch (Exception $e) { return self::exception_handler($e, self::disconnect($conn)); }
	}

	static public function receive($conn = null, $start = 0, $length = null) {
		try {
			$errors = array();
			if (!($conn && is_resource($conn))) $errors[] = 'invalid resource connection';
			if (!is_int($start)) $errors[] = 'invalid start type';
			if (!($length == null || is_int($length))) $errors[] = 'invalid length type';
			if (count($errors) == 0) {
				$msgs = self::pstat($conn);
				$arr = array();
				if ($msgs && $msgs[0] > 0) {
					$sub = '';
					for ($i = 1; $i <= $msgs[0]; $i++) $sub .= $i;
					$res = ($length != null) ? substr($sub, $start, $length) : substr($sub, $start);
					if ($res != '') {
						$begin = intval($res[0]);
						$end = intval($res[strlen($res)-1]);
						for ($i = $begin; $i <= $end; $i++) if ($recv = self::pretr($conn, $i)) $arr[$i] = $recv;
					}
				}
				return (count($arr) > 0) ? $arr : false;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

}

?>