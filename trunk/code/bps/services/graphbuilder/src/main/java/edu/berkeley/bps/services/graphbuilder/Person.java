package edu.berkeley.bps.services.graphbuilder;

import java.util.List;
import java.util.ArrayList;
import edu.berkeley.bps.services.corpus.*;
import edu.berkeley.bps.services.common.LinkTypes;
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
 *    d) (later) co-occurring principles and witnesses, with activity and role type
 *    Note: we do not link to grandfather, but only link to the father,
 *    		who in turn links to the grandfather?
 */
public class Person {
	public static final int EQUAL = 0;
	public static final int COMPAT_LESS_INFO = -1;
	public static final int COMPAT_MORE_INFO = 1;
	public static final int COMPATIBLE = Integer.MIN_VALUE;
	public static final int INCOMPATIBLE = Integer.MAX_VALUE;
	private String displayName = null;
	private Name declaredName = null;
	private Name declaredFather = null;
	private Name declaredGrandFather = null;
	// TODO - rewrite to support an array of ancestors for more cases
	private List<Name> declaredAncestors = null;
	// TODO?? Do we need to model the clan and link to it? What if we are inferring
	// it from various other rules?
	private Name declaredClan = null;
	private PersonLinkSet<Person>	fatherLinks = null;

	protected NameRoleActivity nrad = null;
	private LinkTypes roleInNRAD = null;
	private TimeSpan activeTimeSpan = null;
	// lifeTimeSpan should generally be a DerivedTimeSpan linked to activeTimeSpan.
	private TimeSpan lifeTimeSpan = null;


	public Person(NameRoleActivity nrad, String displayName,
			TimeSpan activeTimeSpan, TimeSpan lifeTimeSpan) {
		this(nrad.getName(), nrad.getFatherName(), nrad.getGrandFatherName(),
				nrad.getAncestorNames(), activeTimeSpan, lifeTimeSpan,
				nrad, LinkTypes.LINK_TO_PERSON, displayName );
	}

	public Person(Name forename, Name father, Name grandfather, List<Name> ancestors,
			TimeSpan activeTimeSpan, TimeSpan lifeTimeSpan,
			NameRoleActivity nrad, LinkTypes roleInNRAD, String displayName ) {
		if(nrad==null)
			throw new IllegalArgumentException("Person ctor must take valid NameRoleActivity.");
		if(activeTimeSpan==null || lifeTimeSpan==null)
			throw new IllegalArgumentException("Person ctor must take valid active and life TimeSpans.");
		this.nrad = nrad;
		this.roleInNRAD = roleInNRAD;
		this.activeTimeSpan = activeTimeSpan;
		this.lifeTimeSpan = lifeTimeSpan;
		declaredName = forename;
		declaredFather = father;
		declaredGrandFather = grandfather;
		declaredAncestors = ancestors;
		fatherLinks = new PersonLinkSet<Person>(this, LinkTypes.LINK_TO_FATHER);
		if(displayName!=null)
			this.displayName = displayName;
		else {
			String suffix = nrad.getDocument().getAlt_id();
			if(suffix==null) {
				suffix = "Doc"+nrad.getDocument().getId();
			}
			this.displayName = ((declaredName!=null)?declaredName:"(unknown)")
								+"."+((suffix!=null)?suffix:nrad.getId());
		}
	}

	public Person createPersonForDeclaredFather(
			long activeTimeSpanOffset, long lifeTimeSpanOffset,
			double activeTimeSpanStdDev, double lifeTimeSpanStdDev,
			boolean addToFatherLinks) {
		if(declaredFather==null)
			return null;
		DerivedTimeSpan fatherActiveTimeSpan =
			new DerivedTimeSpan(activeTimeSpan, activeTimeSpanOffset, activeTimeSpanStdDev);
		DerivedTimeSpan fatherLifeTimeSpan =
			new DerivedTimeSpan(lifeTimeSpan, lifeTimeSpanOffset, lifeTimeSpanStdDev);
		Name fathersGF = null;
		List<Name> fathersAncestors = null;
		if(declaredAncestors!=null&&!declaredAncestors.isEmpty()) {
			fathersGF = declaredAncestors.get(0);
			if(declaredAncestors.size()>1)
				fathersAncestors = declaredAncestors.subList(1, declaredAncestors.size()-1);
		}
		LinkTypes fatherRoleInNRAD;
		switch(roleInNRAD) {
		case LINK_TO_PERSON:
			fatherRoleInNRAD = LinkTypes.LINK_TO_FATHER; break;
		case LINK_TO_FATHER:
			fatherRoleInNRAD = LinkTypes.LINK_TO_GRANDFATHER; break;
		default:
		//case LinkTypes.LINK_TO_GRANDFATHER:
		//case LinkTypes.LINK_TO_ANCESTOR:
			fatherRoleInNRAD = LinkTypes.LINK_TO_ANCESTOR; break;
		}
		Person father = new Person(declaredFather, declaredGrandFather, fathersGF,
				fathersAncestors, fatherActiveTimeSpan, fatherLifeTimeSpan,
				nrad, fatherRoleInNRAD, displayName );
		if(addToFatherLinks) {
			fatherLinks.addLink(father, 1.0);
			fatherLinks.normalize();
		}
		return father;
	}

	public boolean isDeclaredSimple() {
		return (declaredName!=null) && (getNumQualifiers()==0);
	}

	public int getNumQualifiers() {
		int nQuals = 0;
		if(declaredFather!=null) nQuals++;
		if(declaredClan!=null) nQuals++;
		if(declaredGrandFather!=null) nQuals++;
		if(declaredAncestors!=null) nQuals+= declaredAncestors.size();
		return nQuals;
	}

	/**
	 * @return true if this has a declared forename, and 1 and only one qualifier
	 */
	public boolean isDeclaredPartiallyQualified() {
		return(declaredName!=null) && (1==getNumQualifiers());
	}

	/**
	 * @return true if this has a declared forename, and at least two qualifiers
	 */
	public boolean isDeclaredFullyQualified() {
		return(declaredName!=null) && (getNumQualifiers()>=2);
	}

	public boolean declaredInSameDoc(Person other) {
		return nrad.getDocument().equals(other.nrad.getDocument());
	}

	/**
	 * Check used in logic to collapse equivalent Persons.
	 * @param other Person to compare against
	 * @return true if other came from different doc, this is simple,
	 * 		other is qualified, and simple names EQUAL.
	 */
	public boolean isSimpleNameForQualifiedLocalRef(Person other) {
		return(declaredInSameDoc(other)
			&& isDeclaredSimple() && !other.isDeclaredSimple()
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
			&& !isDeclaredSimple()
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
		}
		int fatherCmp = compareNames(declaredFather, compareTo.declaredFather);
		if(fatherCmp==INCOMPATIBLE)
			return fatherCmp;
		if(composite == COMPATIBLE) {
			composite = fatherCmp;
		} if((composite == EQUAL) && (fatherCmp!=COMPATIBLE)) {
			composite = fatherCmp;
		}
		int grandFatherCmp = compareNames(declaredGrandFather, compareTo.declaredGrandFather);
		if(grandFatherCmp==INCOMPATIBLE)
			return grandFatherCmp;
		if(composite == COMPATIBLE) {
			composite = grandFatherCmp;
		} if((composite == EQUAL) && (grandFatherCmp!=COMPATIBLE)) {
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

	public Name getDeclaredName() {
		return declaredName;
	}

	public void setDeclaredName(Name declaredName) {
		this.declaredName = declaredName;
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

	public String toString() {
		return displayName;
	}

}
