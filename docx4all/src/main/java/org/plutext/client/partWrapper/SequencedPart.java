package org.plutext.client.partWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SequencedPart extends Part {

	/* This class represents an OpenPackaging Part, which
	 * contains an ordered collection of id's. These include:
	 * 
	 * - rels
	 * - comments
	 * - endnotes, footnotes
	 * 
	 * Rels is a bit different, because the id's aren't ordered,
	 * and nor is the "id" a number.
	 * 
	 * We're not interested in what _rels part it is a target of,
	 * or any other semantics.
	 */

	private static Logger log = LoggerFactory.getLogger(SequencedPart.class);

	public SequencedPart() {
	}

	public SequencedPart(org.w3c.dom.Document doc) {
		init(doc);
		log.debug("List element: " + xmlNode.getFirstChild().getFirstChild().getLocalName());
		sequencedElements = xmlNode.getFirstChild().getFirstChild().getChildNodes();
	}

	NodeList sequencedElements;

	public Node getNodeByIndex(int i) {
		return sequencedElements.item(i);
	}

}
