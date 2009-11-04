/**
 *
 */
package bps.services.corpus.main;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author pschmitz
 *
 */
public class XMLUtils {
	public static org.w3c.dom.Document OpenXMLFile(String filename) {
		org.w3c.dom.Document newDoc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			newDoc = builder.parse(filename);
        } catch (SAXParseException spe) {
            // Error generated by the parser
            System.out.println("\n** Parsing error" + ", line " +
                spe.getLineNumber() + ", uri " + spe.getSystemId());
            System.out.println("   " + spe.getMessage());
            // Use the contained exception, if any
            Exception x = spe;
            if (spe.getException() != null) {
                x = spe.getException();
            }
            x.printStackTrace();
        } catch (SAXException sxe) {
            // Error generated by this application
            // (or a parser-initialization error)
            System.out.println("SAX error: " + sxe.getMessage());
            Exception x = sxe;
            if (sxe.getException() != null) {
                x = sxe.getException();
            }
            x.printStackTrace();
		} catch( ParserConfigurationException pce ) {
            System.out.println("Parser Cfg error: " + pce.getMessage());
            pce.printStackTrace();
        } catch (IOException ioe) {
            // I/O error
            System.out.println("IO error: " + ioe.getMessage());
            ioe.printStackTrace();
		} catch( RuntimeException e ) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
		}
		return newDoc;
	}
}
