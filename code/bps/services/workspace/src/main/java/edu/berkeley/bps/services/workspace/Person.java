package edu.berkeley.bps.services.workspace;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Comparator;

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
@XmlRootElement(name="person")
public class Person extends Entity {
	private static final String myClass = "Person";

	private static final String FILIATION_SEPARATOR = " / ";
	
	public static final int DEFAULT_ACTIVE_LIFE_YRS = 15;
	public static final double DEFAULT_GENERATION = 15.0*TimeUtils.APPROX_YEAR_IN_MILLIS;
	public static final double DEFAULT_ACTIVE_LIFE_WINDOW = 
		TimeUtils.getDefaultWindowForActiveLife(DEFAULT_ACTIVE_LIFE_YRS);
	public static final double DEFAULT_ACTIVE_LIFE_STDDEV =
		TimeUtils.getDefaultStdDevForActiveLife(DEFAULT_ACTIVE_LIFE_YRS);

	public static final int EQUAL = 0;
	public static final int COMPAT_LESS_INFO = -1;
	public static final int COMPAT_MORE_INFO = 1;
	public static final int COMPATIBLE = Integer.MIN_VALUE;
	public static final int INCOMPATIBLE = Integer.MAX_VALUE;
	// @XmlElement
	private Name declaredFather = null;
	// @XmlElement
	private Name declaredGrandFather = null;
	private ArrayList<Name> declaredAncestors = null;
	// TODO?? Do we need to model the clan and link to it? What if we are inferring
	// it from various other rules?
	// @XmlElement
	private Name declaredClan = null;
	// Note that we do not link beyond our fathers, since they should link 
	// to their own fathers.
	// TODO - revisit this...
	private EntityLinkSet<Person>	fatherLinks = null;
	private EntityLinkSet<Clan>		clanLinks = null;

	//private LinkType.Type roleInNRAD = null;
	private TimeSpan activeTimeSpan = null;
	// lifeTimeSpan should generally be a DerivedTimeSpan linked to activeTimeSpan.
	//private TimeSpan lifeTimeSpan = null;
	
	
	public static class DisplayNameComparator implements Comparator<Person> {
		public int compare(Person pers1, Person pers2) {
			return pers1.displayName.compareTo(pers2.displayName);
		}
	}
	

	
	protected Person() {
		// Entity superclass Ctor with no args will throw a RuntimeException (MUST have an NRAD)
		//throw new RuntimeException("No-arg Ctor should not be called");
	}

	public static Person CreatePersonFromNRADAsEvidence(NameRoleActivity nrad) {
		long center = nrad.getDocument().getDate_norm();
		EvidenceBasedTimeSpan ts = 
			new EvidenceBasedTimeSpan(center, 
					DEFAULT_ACTIVE_LIFE_STDDEV, DEFAULT_ACTIVE_LIFE_WINDOW);
		return new Person(nrad, ts);
	}

	public Person( NameRoleActivity nrad,
			TimeSpan activeTimeSpan ) { //, TimeSpan lifeTimeSpan ) {
		super(nrad);
		if(activeTimeSpan==null ) // || lifeTimeSpan==null)
			throw new IllegalArgumentException(
					"Person ctor must specify valid TimeSpans.");
		this.activeTimeSpan = activeTimeSpan;
		// this.lifeTimeSpan = lifeTimeSpan;
		{
			NameRoleActivity nradFather=nrad.getFather();
			declaredFather = (nradFather==null)?null:nradFather.getName();
			if(declaredFather!=null) {
				String fatherNameStr = declaredFather.getName();
				if(fatherNameStr==null||fatherNameStr.isEmpty()) {
					declaredFather = null;
				}
			}
		}{
			NameRoleActivity nradGrandFather =nrad.getGrandFather();
			declaredGrandFather = (nradGrandFather==null)?null:nradGrandFather.getName();
			if(declaredGrandFather!=null) {
				String grFatherNameStr=declaredGrandFather.getName();
				if(grFatherNameStr==null||grFatherNameStr.isEmpty()) {
					declaredGrandFather = null;
				}
			}
		}{
			List<NameRoleActivity> ancestorNRADs = nrad.getAncestors();
			declaredAncestors = new ArrayList<Name>();
			if(ancestorNRADs!=null) {
				for(NameRoleActivity nradA:ancestorNRADs) {
					Name ancName = nradA.getName();
					if(ancName!=null) {
						String temp=ancName.getName();
						if(temp!=null&&!temp.isEmpty()) {
							declaredAncestors.add(ancName);
						}
					}
				}
			}
		}{
			NameRoleActivity nradClan=nrad.getClan();
			declaredClan = (nradClan==null)?null:nradClan.getName();
			if(declaredClan!=null) {
				String clanNameStr=declaredClan.getName();
				if(clanNameStr==null||clanNameStr.isEmpty()) {
					declaredClan = null;
				}
			}
		}
		updateDisplayName();
		fatherLinks = new EntityLinkSet<Person>(this, LinkType.Type.LINK_TO_FATHER);
	}
	
	public void setDisplayName(String displayName) {
		super.setDisplayName(displayName);
		updateDisplayName();
	}

	
	protected void updateDisplayName() {
		if(displayName.contains(FILIATION_SEPARATOR))
			return;
		if(declaredFather!=null) {
			String fatherNameStr = declaredFather.getName();
			if(fatherNameStr!=null && !fatherNameStr.isEmpty()) {
				displayName = displayName + FILIATION_SEPARATOR + fatherNameStr; 
			}
		}
		if(declaredGrandFather!=null) {
			String grFatherNameStr=declaredGrandFather.getName();
			if(grFatherNameStr!=null && !grFatherNameStr.isEmpty()) {
				displayName = displayName + FILIATION_SEPARATOR + grFatherNameStr; 
			}
		}
		List<NameRoleActivity> ancestorNRADs = originalNRAD.getAncestors();
		declaredAncestors = new ArrayList<Name>();
		if(ancestorNRADs!=null) {
			for(NameRoleActivity nradA:ancestorNRADs) {
				Name ancName = nradA.getName();
				if(ancName!=null) {
					String temp=ancName.getName();
					if(temp!=null && !temp.isEmpty()) {
						displayName = displayName + FILIATION_SEPARATOR + ancName; 
					}
				}
			}
		}
		if(declaredClan!=null) {
			String clanNameStr=declaredClan.getName();
			if(clanNameStr!=null && !clanNameStr.isEmpty()) {
				displayName = displayName + FILIATION_SEPARATOR + clanNameStr; 
			}
		}

	}
	
	public double getDateOverlapLikelihood(Person other) {
		return this.activeTimeSpan.computeMutualProbability(other.activeTimeSpan);
	}
	
	@XmlElement(name="floruit")
	public String getFloruit() {
		return this.activeTimeSpan.getDisplayString();
	}

	/**
	 * Must be called from Workspace context where the values for the 
	 * generational offsets and standard deviations are known
	 * @param activeTimeSpanOffset
	 * @param lifeTimeSpanOffset
	 * @param activeTimeSpanStdDev
	 * @param lifeTimeSpanStdDev
	 * @param addToFatherLinks
	 * @return
	 */
	public Person createPersonForDeclaredFather(
			long activeTimeSpanOffset, // long lifeTimeSpanOffset,
			double activeTimeSpanStdDev, // double lifeTimeSpanStdDev,
			double activeTimeSpanWindow,
			boolean addToFatherLinks) {
		//if(declaredFather==null)		// There can be fathers with missing names...
		//	return null;
		NameRoleActivity nradFather = originalNRAD.getFather(); 
		if(nradFather==null)
			throw new RuntimeException(myClass+"Internal logic error in Person");
		DerivedTimeSpan fatherActiveTimeSpan =
			new DerivedTimeSpan(activeTimeSpan, activeTimeSpanOffset, 
					activeTimeSpanStdDev, activeTimeSpanWindow);
		//DerivedTimeSpan fatherLifeTimeSpan =
		//	new DerivedTimeSpan(lifeTimeSpan, lifeTimeSpanOffset, lifeTimeSpanStdDev);
		/*
		LinkType.Type fatherRoleInNRAD;
		switch(roleInNRAD) {
		case LINK_TO_PERSON:
			fatherRoleInNRAD = LinkType.Type.LINK_TO_FATHER; break;
		case LINK_TO_FATHER:
			fatherRoleInNRAD = LinkType.Type.LINK_TO_GRANDFATHER; break;
		default:
		//case LinkTypes.LINK_TO_GRANDFATHER:
		//case LinkTypes.LINK_TO_ANCESTOR:
			fatherRoleInNRAD = LinkType.Type.LINK_TO_ANCESTOR; break;
		}
		*/
		Person father = new Person(nradFather, fatherActiveTimeSpan); // , fatherLifeTimeSpan);
		if(addToFatherLinks) {
			EntityLink<Person> link = new EntityLink<Person>(this, father, 1.0, LinkType.Type.LINK_TO_FATHER);
			fatherLinks.put(father, link);
			fatherLinks.normalize();
		}
		return father;
	}

	public boolean isUnfiliated() {
		// Must have name, but neither of Father or Clan
		return(declaredName!=null) && 
				(declaredFather==null) && (declaredClan==null);

	}

	public int getNumQualifiers() {
		int nQuals = 0;
		// TODO Consider making Father and Clan worth 2, so that
		// Father and Clan is worth more than Father and Grandfather
		// Talk to Laurie about this - does it really make sense?
		if(declaredFather!=null) nQuals++;
		if(declaredClan!=null) nQuals++;
		if(declaredGrandFather!=null) nQuals++;
		if(declaredAncestors!=null) nQuals+= declaredAncestors.size();
		return nQuals;
	}

	/**
	 * @return true if this has a declared forename, and 1 and only one qualifier
	 */
	public boolean isPartiallyFiliated() {
		// Must have name, but exactly one of Father or Clan
		return(declaredName!=null) && 
			((declaredFather!=null) != (declaredClan!=null));
	}

	/**
	 * @return true if this has a declared forename, and at least two qualifiers
	 */
	public boolean isFullyFiliated() {
		return(declaredName!=null) && 
			(declaredFather!=null) && (declaredClan!=null);
	}

	public boolean declaredInSameDoc(Person other) {
		return originalNRAD.getDocument().equals(other.originalNRAD.getDocument());
	}

	/**
	 * Check used in logic to collapse equivalent Persons.
	 * @param other Person to compare against
	 * @return true if other came from different doc, this is simple,
	 * 		other is qualified, and simple names EQUAL.
	 */
	public boolean isSimpleNameForQualifiedLocalRef(Person other) {
		return(declaredInSameDoc(other)
			&& isUnfiliated() && !other.isUnfiliated()
			&& (EQUAL==compareNames(declaredName, other.declaredName)));
	}

	/**
	 * Check used in logic to collapse equivalent Persons.
	 * @param other Person to compare against
	 * @return true if other came from same doc, and this is qualified,
	 * 		compatible, but less qualified than other.
	 */
	public boolean isLessQualifiedNameForLocalRef(Person other) {
		return (declaredInSameDoc(other)
			&& !isUnfiliated()
			&& (COMPAT_LESS_INFO==compareNames(declaredName, other.declaredName)));
	}

	/**
	 * Compares this Person to another, checking declared Names, clans, etc.
	 * @param compareTo
	 * @return EQUAL if equal,
	 *    COMPATIBLE if all fields are null
	 *    COMPAT_MORE_INFO if this equals fields in compareTo, but this has more clan or ancestor info,
	 *    COMPAT_LESS_INFO if this equals fields in compareTo, but compareTo has more clan or ancestor info.
	 *    INCOMPATIBLE if any fields conflict.
	 */
	public int compareByNames(Person compareTo) {
		int composite;
		// Check all fields for conflicts, returning INCOMPATIBLE if any conflicts
		int nameCmp = compareNames(declaredName, compareTo.declaredName);
		if(nameCmp==INCOMPATIBLE)
			return nameCmp;
		composite = nameCmp;
		int clanCmp = compareNames(declaredClan, compareTo.declaredClan);
		if(clanCmp==INCOMPATIBLE)
			return clanCmp;
		// If we're (only) compatible so far, use clan result for composite
		if(composite == COMPATIBLE) {
			composite = clanCmp;
		}
		// If we're equal so far, and if clan is compatible then stay equal,
		// otherwise, set composite to the clan comparison (COMPAT_MORE or COMPAT_LESS)
		else if((composite == EQUAL) && (clanCmp!=COMPATIBLE)) {
			composite = clanCmp;
		} // Else we stay with the composite
		// TODO what if composite was COMPAT_MORE and clanCmp is COMPAT_LESS ?
		int fatherCmp = compareNames(declaredFather, compareTo.declaredFather);
		if(fatherCmp==INCOMPATIBLE)
			return fatherCmp;
		if(composite == COMPATIBLE) {
			composite = fatherCmp;
		} else if((composite == EQUAL) && (fatherCmp!=COMPATIBLE)) {
			composite = fatherCmp;
		}
		int grandFatherCmp = compareNames(declaredGrandFather, compareTo.declaredGrandFather);
		if(grandFatherCmp==INCOMPATIBLE)
			return grandFatherCmp;
		if(composite == COMPATIBLE) {
			composite = grandFatherCmp;
		} else if((composite == EQUAL) && (grandFatherCmp!=COMPATIBLE)) {
			composite = grandFatherCmp;
		}
		// We're compatible so far
		// TODO compare ancestors
		return composite;
	}

	/**
	 * Compares two names
	 * @param name1
	 * @param name2
	 * @return EQUAL if both are non-null and equal,
	 *		   COMPATIBLE if both are null
	 *         COMPAT_MORE_INFO if name1 is non-null and name2 is null,
	 *         COMPAT_LESS_INFO if name1 is null and name2 is non-null,
	 *         INCOMPATIBLE if both are non-null and not equal.
	 */
	private static int compareNames(Name name1, Name name2) {
		if(name1!=null) {
			return(name2==null)?
					COMPAT_MORE_INFO:(name1.equals(name2)?EQUAL:INCOMPATIBLE);
		} else {	// name1 is null
			return(name2!=null)? COMPAT_LESS_INFO:COMPATIBLE;
		}
	}

	public Name getDeclaredFather() {
		return declaredFather;
	}

	public void setDeclaredFather(Name declaredFather) {
		this.declaredFather = declaredFather;
	}

	public Name getDeclaredGrandFather() {
		return declaredGrandFather;
	}

	/**
	 * @param ancestor
	 * @return true (as specified by Collection.add(E))
	 */
	public boolean addAncestor(Name ancestor) {
		return declaredAncestors.add(ancestor);
	}

	public int getNAncestors() {
		return declaredAncestors.size();
	}

	public Name getAncestor(int index) {
		return declaredAncestors.get(index);
	}

}
