package edu.berkeley.bps.services.workspace;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.berkeley.bps.services.common.LinkType;
import edu.berkeley.bps.services.corpus.NameRoleActivity;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="nradToEntityLink")
public class NRADEntityLink extends EntityLink<NameRoleActivity> {
	
	public NRADEntityLink() {
		super();
	}
	
	public NRADEntityLink(NameRoleActivity fromNRAD, Entity linkTo, double weight, LinkType.Type linkType) {
		super(fromNRAD, linkTo, weight, linkType);
	}

	/**
	 * @return the nradId
	 */
	@XmlElement(name="nradId")
	public int getFromId() {
		return fromObj.getId();
	}

}
