<?PHP
///////////////////////////////////////////////////////////////////////////
//                                                                       //
// BPS configuration file                                                //
//                                                                       //
// This file should be renamed "config.php" in the top-level directory   //
//                                                                       //
///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
//                                                                       //
// This program is free software; you can redistribute it and/or modify  //
// it under the terms of the GNU General Public License as published by  //
// the Free Software Foundation; either version 2 of the License, or     //
// (at your option) any later version.                                   //
//                                                                       //
// This program is distributed in the hope that it will be useful,       //
// but WITHOUT ANY WARRANTY; without even the implied warranty of        //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
// GNU General Public License for more details:                          //
//                                                                       //
//          http://www.gnu.org/copyleft/gpl.html                         //
//                                                                       //
///////////////////////////////////////////////////////////////////////////
unset($CFG);  // Ignore this line

//=========================================================================
// 1. DATABASE SETUP
//=========================================================================
// First, you need to configure the database where all BPS data          //
// will be stored.  This database must already have been created         //
// and a username/password created to access it.                         //
//                                                                       //
//   mysql      - the prefix is optional, but useful when installing     //
//                into databases that already contain tables.            //
//                                                                       //
//

$CFG->dbtype    = 'mysql';       // mysql
$CFG->dbhost    = 'localhost';   // eg localhost or db.isp.com
$CFG->dbname    = 'bpsdev';      // database name
$CFG->dbuser    = 'bpsdev';    // your database username
$CFG->dbpass    = 'G0Names!';    // your database password
$CFG->prefix    = '';        // Prefix to use for all table names (not working yet)


//=========================================================================
// 2. WEB SITE LOCATION
//=========================================================================
// Now you need to tell BPS where it is located. Specify the full
// web address to where BPS has been installed.  If your web site
// is accessible via multiple URLs then choose the most natural one.
//
// Do not include a trailing slash!

$CFG->wwwroot   = 'http://'.$_SERVER['SERVER_NAME'];


//=========================================================================
// 2.1 SHORT URL BASE
//=========================================================================
// shortbase allows BPS to construct short URLs in conjunction with Apache rewite rules.
//

$CFG->shortbase   = '/';

//=========================================================================
// 2.2 SERVICES URL BASE
//=========================================================================
// svcsbase allows BPS to construct calls to services provided by Tomcat, JBoss, etc.
// This must map the JkMount point declared for the VirtualHost.
//
// Do not include a trailing slash!

$CFG->svcsbase   = '/bps.services.webapp';

//=========================================================================
// 3. SERVER FILES LOCATION
//=========================================================================
// Next, specify the full OS directory path to this same location
// Make sure the upper/lower case is correct.  Some examples:
//
//    $CFG->dirroot = 'c:\program files\easyphp\www\delphi';    // Windows
//    $CFG->dirroot = '/var/www/html/delphi';     // Redhat Linux
//    $CFG->dirroot = '/home/example/public_html/delphi'; // Cpanel host

$CFG->dirroot           = '/var/www';


//=========================================================================
// 4. CONTACT EMAIL
//=========================================================================
// Specify an email address to recieve messages from the contact form
//

$CFG->contactEmail      = "bps_feedback@lists.berkeley.edu";

//=========================================================================
// 5. CORPUS FILE DIRECTORY
//=========================================================================
// Here, specify the full OS directory path to the root directory
// for corpus files (which will be stored by ID underneath). 
// Make sure the upper/lower case is correct.  Some examples:
//
//    $CFG->corpusdir = 'c:\bps\corpora';    // Windows
//    $CFG->corpusdir = '/var/bps/corpora';     // Linux
//
// Do not include a trailing slash!


$CFG->corpusdir       = "/var/bps/corpora";


//=========================================================================
// 9. General UI defaults and constants
//=========================================================================
// Specify the following general constants
//
// Specify a default web page title. Can be overridden by individual pages.
$CFG->page_title_default    = "Berkeley Prosopography Services";


//=========================================================================
// ALL DONE!  To continue installation, visit your main page with a browser
//=========================================================================



if ($CFG->wwwroot == 'http://example.com/') {
    echo "<p>Error detected in configuration file</p>";
    echo "<p>Your server address can not be: \$CFG->wwwroot = 'http://example.com/';</p>";
    die;
}

?>
