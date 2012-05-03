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

if (!class_exists('XPM3_FUNC')) require_once 'XPM3_FUNC.php';

class XPM3_MIME extends XPM3_FUNC {

	const CRLF = "\r\n"; // PHP_EOL
	const HLEN = 52;
	const MLEN = 74;

	const HCHARSET = 'utf-8';
	const MCHARSET = 'us-ascii';

	const HENCODING = 'quoted-printable';
	const MENCODING = 'quoted-printable';

	static public $_hencoding = array('quoted-printable' => '', 'base64' => '');
	static public $_mencoding = array('7bit' => '', '8bit' => '', 'quoted-printable' => '', 'base64' => '');

	static public $_qpkeys = array(
			"\x00","\x01","\x02","\x03","\x04","\x05","\x06","\x07",
			"\x08","\x09","\x0A","\x0B","\x0C","\x0D","\x0E","\x0F",
			"\x10","\x11","\x12","\x13","\x14","\x15","\x16","\x17",
			"\x18","\x19","\x1A","\x1B","\x1C","\x1D","\x1E","\x1F",
			"\x7F","\x80","\x81","\x82","\x83","\x84","\x85","\x86",
			"\x87","\x88","\x89","\x8A","\x8B","\x8C","\x8D","\x8E",
			"\x8F","\x90","\x91","\x92","\x93","\x94","\x95","\x96",
			"\x97","\x98","\x99","\x9A","\x9B","\x9C","\x9D","\x9E",
			"\x9F","\xA0","\xA1","\xA2","\xA3","\xA4","\xA5","\xA6",
			"\xA7","\xA8","\xA9","\xAA","\xAB","\xAC","\xAD","\xAE",
			"\xAF","\xB0","\xB1","\xB2","\xB3","\xB4","\xB5","\xB6",
			"\xB7","\xB8","\xB9","\xBA","\xBB","\xBC","\xBD","\xBE",
			"\xBF","\xC0","\xC1","\xC2","\xC3","\xC4","\xC5","\xC6",
			"\xC7","\xC8","\xC9","\xCA","\xCB","\xCC","\xCD","\xCE",
			"\xCF","\xD0","\xD1","\xD2","\xD3","\xD4","\xD5","\xD6",
			"\xD7","\xD8","\xD9","\xDA","\xDB","\xDC","\xDD","\xDE",
			"\xDF","\xE0","\xE1","\xE2","\xE3","\xE4","\xE5","\xE6",
			"\xE7","\xE8","\xE9","\xEA","\xEB","\xEC","\xED","\xEE",
			"\xEF","\xF0","\xF1","\xF2","\xF3","\xF4","\xF5","\xF6",
			"\xF7","\xF8","\xF9","\xFA","\xFB","\xFC","\xFD","\xFE",
			"\xFF");

	static public $_qpvrep = array(
			"=00","=01","=02","=03","=04","=05","=06","=07",
			"=08","=09","=0A","=0B","=0C","=0D","=0E","=0F",
			"=10","=11","=12","=13","=14","=15","=16","=17",
			"=18","=19","=1A","=1B","=1C","=1D","=1E","=1F",
			"=7F","=80","=81","=82","=83","=84","=85","=86",
			"=87","=88","=89","=8A","=8B","=8C","=8D","=8E",
			"=8F","=90","=91","=92","=93","=94","=95","=96",
			"=97","=98","=99","=9A","=9B","=9C","=9D","=9E",
			"=9F","=A0","=A1","=A2","=A3","=A4","=A5","=A6",
			"=A7","=A8","=A9","=AA","=AB","=AC","=AD","=AE",
			"=AF","=B0","=B1","=B2","=B3","=B4","=B5","=B6",
			"=B7","=B8","=B9","=BA","=BB","=BC","=BD","=BE",
			"=BF","=C0","=C1","=C2","=C3","=C4","=C5","=C6",
			"=C7","=C8","=C9","=CA","=CB","=CC","=CD","=CE",
			"=CF","=D0","=D1","=D2","=D3","=D4","=D5","=D6",
			"=D7","=D8","=D9","=DA","=DB","=DC","=DD","=DE",
			"=DF","=E0","=E1","=E2","=E3","=E4","=E5","=E6",
			"=E7","=E8","=E9","=EA","=EB","=EC","=ED","=EE",
			"=EF","=F0","=F1","=F2","=F3","=F4","=F5","=F6",
			"=F7","=F8","=F9","=FA","=FB","=FC","=FD","=FE",
			"=FF");

	static public function unique($add = null) {
		return md5(microtime(1).$add);
	}

	static public function is_printable($str = null) {
		try {
			if (is_string($str) && $str != '') {
				$contain = implode('', self::$_qpkeys);
				return (strcspn($str, $contain) == strlen($str));
			} else throw new Exception('invalid argument type', 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function qp_encode($str = null, $len = self::MLEN, $end = self::CRLF) {
		try {
			$errors = array();
			if (!(is_string($str) && $str != '')) $errors[] = 'invalid argument type';
			if (!(is_int($len) && $len > 1)) $errors[] = 'invalid line length value';
			if (!is_string($end)) $errors[] = 'invalid line end value';
			if (count($errors) == 0) {
				$out = '';
				foreach (explode($end, $str) as $line) {
					$line = str_replace('=', '=3D', $line);
					$line = str_replace(self::$_qpkeys, self::$_qpvrep, $line);
					preg_match_all('/.{1,'.$len.'}([^=]{0,2})?/', $line, $match);
					$mcnt = count($match[0]);
					for ($i = 0; $i < $mcnt; $i++) {
						$line = (substr($match[0][$i], -1) == ' ') ? substr($match[0][$i], 0, -1).'=20' : $match[0][$i];
						if (($i+1) < $mcnt) $line .= '=';
						$out .= $line.$end;
					}
				}
				return ($out == '') ? '' : substr($out, 0, -strlen($end));
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
    }

	static public function encode_header($str = null, $charset = self::HCHARSET, $encoding = self::HENCODING) {
		try {
			$errors = array();
			if (!(is_string($str) && $str != '')) $errors[] = 'invalid argument type';
			if (!is_string($charset)) $errors[] = 'invalid charset type';
			else {
				$charset = self::str_clear($charset, array(' '));
				$charlen = strlen($charset);
				if ($charlen < 4 || $charlen > 22) $errors[] = 'invalid charset value';
			}
			if (!(is_string($encoding) && isset(self::$_hencoding[$encoding]))) $errors[] = 'invalid encoding value';
			if (count($errors) == 0) {
				$enc = false;
				if ($encoding == 'quoted-printable') {
					if (!self::is_printable($str)) {
						$enc = self::qp_encode($str, self::HLEN);
						$enc = str_replace('?', '=3F', $enc);
					}
				} else if ($encoding == 'base64') $enc = rtrim(chunk_split(base64_encode($str), self::HLEN, self::CRLF));
				if ($enc) {
					$res = array();
					$chr = ($encoding == 'base64') ? 'B' : 'Q';
					foreach (explode(self::CRLF, $enc) as $val) if ($val != '') $res[] = '=?'.$charset.'?'.$chr.'?'.$val.'?=';
					return implode(self::CRLF."\t", $res);
				} else return $str;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function decode_header($str = null) {
		try {
			if (is_string($str)) {
				$out = '';
				$set = 'ISO-8859-1';
				$enc = '7bit';
				$new = self::is_win() ? "\r\n" : "\n";
				foreach (explode($new."\t", $str) as $line) {
					$dec = false;
					if (strlen($line) > 10 && substr($line, 0, 2) == '=?' && substr($line, -2) == '?=') {
						if ($code = stristr($line, '?Q?')) {
							$exp = explode('?Q?', $line);
							$chr = substr($exp[0], 2, strlen($exp[0]));
							if (strlen($chr) > 3) {
								$set = $chr;
								$enc = 'quoted-printable';
								$sub = substr($code, 3, -2);
								$dec = self::is_printable($sub) ? quoted_printable_decode($sub) : $sub;
							}
						} else if ($code = stristr($line, '?B?')) {
							$exp = explode('?B?', $line);
							$chr = substr($exp[0], 2, strlen($exp[0]));
							if (strlen($chr) > 3) {
								$set = $chr;
								$enc = 'base64';
								$dec = base64_decode(substr($code, 3, -2));
							}
						}
					}
					$out .= $dec ? $dec : $line;
				}
				return array('charset' => $set, 'encoding' => $enc, 'value' => $out);
			} else throw new Exception('invalid argument type', 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function decode_content($str = null, $decoding = '7bit') {
		try {
			$errors = array();
			if (!is_string($str)) $errors[] = 'invalid argument type';
			if (!is_string($decoding)) $errors[] = 'invalid decoding type';
			else {
				$decoding = self::str_clear($decoding, array(' '));
				$decoding = strtolower($decoding);
				if (!isset(self::$_mencoding[$decoding])) $errors[] = 'invalid decoding value';
			}
			if (count($errors) == 0) {
				if ($decoding == 'base64') {
					$str = self::str_clear($str);
					return trim(base64_decode($str));
				} else if ($decoding == 'quoted-printable') {
					return quoted_printable_decode($str);
				} else return $str;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function message($content = null, $type = null, $name = null, $charset = null, $encoding = null, $disposition = null, $id = null, $exception = null) {
		try {
			$errors = array();
			if (!(is_string($content) && $content != '')) $errors[] = 'invalid content type';
			if ($type == null) $type = 'application/octet-stream';
			else if (is_string($type)) {
				$type = self::str_clear($type);
				$type = trim($type);
				$typelen = strlen($type);
				if ($typelen < 4 || $typelen > 64) $errors[] = 'invalid type value';
			} else $errors[] = 'invalid type';
			if ($name == null) $name = '';
			else if (is_string($name)) {
				$name = self::str_clear($name);
				$name = trim($name);
				if ($name != '') {
					$namelen = strlen($name);
					if ($namelen < 2 || $namelen > 64) $errors[] = 'invalid name value';
				}
			} else $errors[] = 'invalid name type';
			if ($charset == null) $charset = '';
			else if (is_string($charset)) {
				$charset = self::str_clear($charset, array(' '));
				if ($charset != '') {
					$charlen = strlen($charset);
					if ($charlen < 4 || $charlen > 64) $errors[] = 'invalid charset value';
				}
			} else $errors[] = 'invalid charset type';
			if ($encoding == null) $encoding = self::MENCODING;
			else if (is_string($encoding)) {
				$encoding = self::str_clear($encoding, array(' '));
				$encoding = strtolower($encoding);
				if (!isset(self::$_mencoding[$encoding])) $errors[] = 'invalid encoding value';
			} else $errors[] = 'invalid encoding type';
			if ($disposition == null) $disposition = 'inline';
			else if (is_string($disposition)) {
				$disposition = self::str_clear($disposition, array(' '));
				$disposition = strtolower($disposition);
				if (!($disposition == 'inline' || $disposition == 'attachment')) $errors[] = 'invalid disposition value';
			} else $errors[] = 'invalid disposition type';
			if ($id == null) $id = '';
			else if (is_string($id)) {
				$id = self::str_clear($id, array(' '));
				if ($id != '') {
					$idlen = strlen($id);
					if ($idlen < 2 || $idlen > 64) $errors[] = 'invalid id value';
				}
			} else $errors[] = 'invalid id type';
			if (count($errors) == 0) {
				$header = 'Content-Type: '.$type.
					(($charset != '') ? ';'.self::CRLF."\t".'charset="'.$charset.'"' : '').self::CRLF.
					'Content-Transfer-Encoding: '.$encoding.self::CRLF.
					'Content-Disposition: '.$disposition.
					(($name != '') ? ';'.self::CRLF."\t".'filename="'.$name.'"' : '').
					(($id != '') ? self::CRLF.'Content-ID: <'.$id.'>' : '');
				if ($encoding == '7bit' || $encoding == '8bit') $content = wordwrap($content, self::MLEN, self::CRLF, true);
				else if ($encoding == 'base64') $content = chunk_split(base64_encode($content), self::MLEN, self::CRLF);
				else if ($encoding == 'quoted-printable') $content = self::qp_encode($content);
				return array('name' => $name, 'disposition' => $disposition, 'header' => $header, 'content' => $content);
			} else throw self::exception_rewrite($exception, implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function compose($text = null, $html = null, $attach = null, $uniq = null) {
		try {
			$errors = array();
			if ($text == null && $html == null) $errors[] = 'message is not set';
			else {
				if ($text != null) {
					if (!(is_array($text) && isset($text['header'], $text['content']) && is_string($text['header']) && is_string($text['content']))) $errors[] = 'invalid text message format';
				}
				if ($html != null) {
					if (!(is_array($html) && isset($html['header'], $html['content']) && is_string($html['header']) && is_string($html['content']))) $errors[] = 'invalid html message format';
				}
				if ($attach != null) {
					if (is_array($attach) && count($attach) > 0) {
						foreach ($attach as $arr) {
							if (!(is_array($arr) && isset($arr['disposition'], $arr['header'], $arr['content']) && is_string($arr['header']) && is_string($arr['content']))) {
								$errors[] = 'invalid attachment format';
								break;
							}
						}
					} else $errors[] = 'invalid attachment format';
				}
			}
			if (count($errors) == 0) {
				$multipart = false;
				if ($text && $html) $multipart = true;
				if ($attach) $multipart = true;
				$addheader = array();
				$body = '';
				if ($multipart) {
					$uniq = ($uniq == null) ? 0 : intval($uniq);
					$boundary1 = '=_'.self::unique($uniq++);
					$boundary2 = '=_'.self::unique($uniq++);
					$boundary3 = '=_'.self::unique($uniq++);
					$disp['inline'] = $disp['attachment'] = false;
					if ($attach) {
						foreach ($attach as $desc) {
							if ($desc['disposition'] == 'inline') $disp['inline'] = true;
							else $disp['attachment'] = true;
						}
					}
					$addheader[] = 'MIME-Version: 1.0';
					$body = 'This is a message in MIME Format. If you see this, your mail reader does not support this format.'.self::CRLF.self::CRLF;
					if ($text && $html) {
						if ($disp['inline'] && $disp['attachment']) {
							$addheader[] = 'Content-Type: multipart/mixed;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
							$body .= '--'.$boundary1.self::CRLF.'Content-Type: multipart/related;'.self::CRLF."\t".'boundary="'.$boundary2.'"'.self::CRLF.self::CRLF.
								'--'.$boundary2.self::CRLF.'Content-Type: multipart/alternative;'.self::CRLF."\t".'boundary="'.$boundary3.'"'.self::CRLF.self::CRLF.
								'--'.$boundary3.self::CRLF.$text['header'].self::CRLF.self::CRLF.$text['content'].self::CRLF.
								'--'.$boundary3.self::CRLF.$html['header'].self::CRLF.self::CRLF.$html['content'].self::CRLF.
								'--'.$boundary3.'--'.self::CRLF;
							foreach ($attach as $desc) if ($desc['disposition'] == 'inline') $body .= '--'.$boundary2.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary2.'--'.self::CRLF;
							foreach ($attach as $desc) if ($desc['disposition'] == 'attachment') $body .= '--'.$boundary1.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary1.'--';
						} else if ($disp['inline']) {
							$addheader[] = 'Content-Type: multipart/related;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
							$body .= '--'.$boundary1.self::CRLF.'Content-Type: multipart/alternative;'.self::CRLF."\t".'boundary="'.$boundary2.'"'.self::CRLF.self::CRLF.
								'--'.$boundary2.self::CRLF.$text['header'].self::CRLF.self::CRLF.$text['content'].self::CRLF.
								'--'.$boundary2.self::CRLF.$html['header'].self::CRLF.self::CRLF.$html['content'].self::CRLF.
								'--'.$boundary2.'--'.self::CRLF;
							foreach ($attach as $desc) $body .= '--'.$boundary1.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary1.'--';
						} else if ($disp['attachment']) {
							$addheader[] = 'Content-Type: multipart/mixed;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
							$body .= '--'.$boundary1.self::CRLF.'Content-Type: multipart/alternative;'.self::CRLF."\t".'boundary="'.$boundary2.'"'.self::CRLF.self::CRLF.
								'--'.$boundary2.self::CRLF.$text['header'].self::CRLF.self::CRLF.$text['content'].self::CRLF.
								'--'.$boundary2.self::CRLF.$html['header'].self::CRLF.self::CRLF.$html['content'].self::CRLF.
								'--'.$boundary2.'--'.self::CRLF;
							foreach ($attach as $desc) $body .= '--'.$boundary1.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary1.'--';
						} else {
							$addheader[] = 'Content-Type: multipart/alternative;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
							$body .= '--'.$boundary1.self::CRLF.$text['header'].self::CRLF.self::CRLF.$text['content'].self::CRLF.
								'--'.$boundary1.self::CRLF.$html['header'].self::CRLF.self::CRLF.$html['content'].self::CRLF.
								'--'.$boundary1.'--';
						}
					} else if ($text) {
						$addheader[] = 'Content-Type: multipart/mixed;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
						$body .= '--'.$boundary1.self::CRLF.$text['header'].self::CRLF.self::CRLF.$text['content'].self::CRLF;
						foreach ($attach as $desc) $body .= '--'.$boundary1.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
						$body .= '--'.$boundary1.'--';
					} else if ($html) {
						if ($disp['inline'] && $disp['attachment']) {
							$addheader[] = 'Content-Type: multipart/mixed;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
							$body .= '--'.$boundary1.self::CRLF.'Content-Type: multipart/related;'.self::CRLF."\t".'boundary="'.$boundary2.'"'.self::CRLF.self::CRLF.
								'--'.$boundary2.self::CRLF.$html['header'].self::CRLF.self::CRLF.$html['content'].self::CRLF;
							foreach ($attach as $desc) if ($desc['disposition'] == 'inline') $body .= '--'.$boundary2.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary2.'--'.self::CRLF;
							foreach ($attach as $desc) if ($desc['disposition'] == 'attachment') $body .= '--'.$boundary1.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary1.'--';
						} else if ($disp['inline']) {
							$addheader[] = 'Content-Type: multipart/related;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
							$body .= '--'.$boundary1.self::CRLF.$html['header'].self::CRLF.self::CRLF.$html['content'].self::CRLF;
							foreach ($attach as $desc) $body .= '--'.$boundary1.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary1.'--';
						} else if ($disp['attachment']) {
							$addheader[] = 'Content-Type: multipart/mixed;'.self::CRLF."\t".'boundary="'.$boundary1.'"';
							$body .= '--'.$boundary1.self::CRLF.$html['header'].self::CRLF.self::CRLF.$html['content'].self::CRLF;
							foreach ($attach as $desc) $body .= '--'.$boundary1.self::CRLF.$desc['header'].self::CRLF.self::CRLF.$desc['content'].self::CRLF;
							$body .= '--'.$boundary1.'--';
						}
					}
				} else {
					if ($text) {
						$addheader[] = $text['header'];
						$body = $text['content'];
					} else if ($html) {
						$addheader[] = $html['header'];
						$body = $html['content'];
					}
				}
				return array('addheader' => implode(self::CRLF, $addheader), 'body' => $body);
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function split_message($body = null, $multipart = null, $boundary = null) {
		try {
			$errors = array();
			if (!is_string($body)) $errors[] = 'invalid body type';
			if (!is_string($multipart)) $errors[] = 'invalid multipart type';
			if (!is_string($boundary)) $errors[] = 'invalid boundary type';
			if (count($errors) == 0) {
				$ret = array();
				if (strstr($body, '--'.$boundary.'--')) {
					$exp1 = explode('--'.$boundary.'--', $body);
					$new = self::is_win() ? "\r\n" : "\n";
					if (strstr($exp1[0], '--'.$boundary.$new)) {
						foreach (explode('--'.$boundary.$new, $exp1[0]) as $part) {
							if ($data1 = stristr($part, 'content-type: ')) {
								if ($data2 = stristr($part, 'boundary=')) {
									$exp2 = explode('multipart/', $part);
									$exp3 = explode(';', $exp2[1]);
									$multipart2 = trim(strtolower($exp3[0]));
									if ($multipart2 == 'mixed' || $multipart2 == 'related' || $multipart2 == 'alternative') {
										$data2 = substr($data2, strlen('boundary='));
										$exp4 = explode("\n", $data2);
										$exp5 = explode("\r", $exp4[0]);
										$boundary2 = trim($exp5[0], '"');
										if ($boundary2 != '') $ret = self::split_message($part, $multipart.', '.$multipart2, $boundary2);
									}
								} else {
									if ($res = self::split_compose($part)) {
										$one = array();
										foreach ($res['header'] as $harr) {
											foreach ($harr as $hnum => $hval) if (stristr($hnum, 'content-')) $one[$hnum] = $hval;
										}
										$one['multipart'] = $multipart;
										$one['data'] = $res['body'];
										$ret[] = $one;
									}
								}
							}
						}
					}
				}
				return (count($ret) > 0) ? $ret : false;
			} else throw new Exception(implode(', ', $errors), 0);
		} catch (Exception $e) { return self::exception_handler($e); }
	}

	static public function split_compose($str = null) {
		try {
			if (!is_string($str)) throw new Exception('invalid argument type', 0);
			$new = self::is_win() ? "\r\n" : "\n";
			$sep = $new.$new;
			$arr['header'] = $arr['body'] = array();
			if (!(count($exp1 = explode($sep, $str)) > 1)) {
				$new = ($new == "\n") ? "\r\n" : "\n";
				$sep = $new.$new;
				if (!(count($exp1 = explode($sep, $str)) > 1)) throw new Exception('invalid 1 argument value', 1);
			}
			$multipart = false;
			$header = str_replace(';'.$new."\t", '; ', $exp1[0]);
			$header = str_replace($new."\t", '', $header);
			if (!(count($exp2 = explode($new, $header)) > 1)) throw new Exception('invalid 2 argument value', 1);
			foreach ($exp2 as $hval) {
				$exp3 = explode(': ', $hval, 2);
				$name = trim($exp3[0]);
				if (count($exp3) == 2 && $name != '' && !strstr($name, ' ')) {
					$sval = trim(self::str_clear($exp3[1]));
					$arr['header'][] = array($name => $sval);
					if (strtolower($name) == 'content-type') {
						if (($data1 = stristr($sval, 'multipart/')) && ($data2 = stristr($sval, 'boundary='))) {
							$data3 = trim(substr($data2, strlen('boundary=')));
							$bexpl = explode(';', $data3);
							$boundary = trim($bexpl[0], '"');
							if ($boundary != '') {
								$data4 = substr($data1, strlen('multipart/'));
								$mexpl = explode(';', $data4);
								$mtype = trim(strtolower($mexpl[0]));
								if ($mtype == 'mixed' || $mtype == 'related' || $mtype == 'alternative') $multipart = $mtype;
							}
						}
					}
				}
			}
			if (count($arr['header']) > 0) {
				if ($multipart) {
					$arr['multipart'] = $multipart;
					$arr['boundary']  = $boundary;
				}
				$body = strstr($str, $sep);
				$body = substr($body, strlen($sep));
				$arr['body'] = $body;
				return $arr;
			} else throw new Exception('invalid 3 argument value', 1);
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

	static public function split_content($str = null) {
		try {
			if (!is_string($str)) throw new Exception('invalid argument type', 0);
			if (!$res = self::split_compose($str)) throw new Exception('invalid 1 argument value', 1);
			$arr = array();
			if (isset($res['multipart'], $res['boundary'])) {
				$arr['header'] = $res['header'];
				$arr['multipart'] = 'yes';
				if (!$arr['body'] = self::split_message($res['body'], $res['multipart'], $res['boundary'])) throw new Exception('invalid 2 argument value', 1);
			} else {
				foreach ($res['header'] as $harr) {
					foreach ($harr as $hnum => $hval) if (stristr($hnum, 'content-')) $content[$hnum] = $hval;
				}
				$content['data'] = $res['body'];
				$arr['header'] = $res['header'];
				$arr['multipart'] = 'no';
				$arr['body'][] = $content;
			}
			return $arr;
		} catch (Exception $e) { return self::exception_handler($e, false); }
	}

}

?>