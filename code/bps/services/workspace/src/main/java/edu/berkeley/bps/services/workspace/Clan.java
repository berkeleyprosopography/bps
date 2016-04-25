package edu.berkeley.bps.services.workspace;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.corpus.*;
import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.common.time.*;

/*
 * TODO Need to model the links from this Person to other Persons, docs, etc.
 * 1) Links to docs (nrad instances) that are associated to this person by virtue
 *    of original citation (for the Person created directly for an nrad) or after
 *    collapse operations (when weight is shifted to other Persons, and so to their
 *    original citing doc/nrad).
 * 2) Links to family relations
 *    a) father as declared, or as shifted to others
 *    b) sons as declared, or as shifted to others
 *    c) Clan?? Perhaps not, or...?
 *    d) (later) co-occurring principals and witnesses, with activity and role type
 *    Note: we do not link to grandfather, but only link to the father,
 *    		who in turn links to the grandfather?
 */
/**
 * @author pschmitz
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="clan")
public class Clan extends Entity {

	public static class DisplayNameComparator implements Comparator<Clan> {
		public int compare(Clan clan1, Clan clan2) {
			return clan1.displayName.compareTo(clan2.displayName);
		}
	}
	
	protected Clan() {
		// Entity superclass Ctor with no args will throw a RuntimeException (MUST have an NRAD)
		//throw new RuntimeException("No-arg Ctor should not be called");
	}

	public Clan(NameRoleActivity nrad) {
		super( nrad );
	}

	@Override
	public int getNumQualifiers() {
		// Clans are not (yet) qualified in any way.
		return 0;
	}

}
