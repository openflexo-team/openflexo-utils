/*
 *  Copyright 2008, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4all.

    Docx4all is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4all is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4all.  If not, see <http://www.gnu.org/licenses/>.

 */

package org.plutext.client.wrappedTransforms;

import java.util.HashMap;

import org.docx4all.xml.ElementMLFactory;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Tag;
import org.plutext.client.Mediator;
import org.plutext.client.SdtWrapper;
import org.plutext.client.state.StateChunk;
import org.plutext.transforms.Changesets.Changeset;
import org.plutext.transforms.Transforms.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransformAbstract {

	private static Logger log = LoggerFactory.getLogger(TransformAbstract.class);

	protected SdtBlock sdt = null;
	protected org.docx4j.wml.SdtBlock markedUpSdt = null;

	private ElementMLFactory factory;

	protected SdtWrapper sdtWrapper;

	public SdtBlock getSdt() {
		return sdt;
	}

	public TransformAbstract() {
	}

	public T t = null;

	public TransformAbstract(T t, ElementMLFactory factory) {
		this.t = t;

		this.factory = factory;

		sequenceNumber = t.getSnum();
		changesetNumber = t.getChangeset();

		sdt = t.getSdt();

		if (t.getIdref() != null) {
			// Case: Delete
			// sdtWrapper = new SdtWrapper();

			// Convert the idref to an id object
			// org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
			// id = factory.createId();
			// id.setVal(BigInteger.valueOf(t.getIdref()));

			// sdtWrapper.setId( t.getIdref().toString() );

		} else if (t.getOp().equals("style")) {

			// No ID
			// sdtWrapper = new SdtWrapper();

		} else {

			// Case: Update, Insert
			sdtWrapper = new SdtWrapper(sdt);

			// id = sdt.getSdtPr().getId();
			// tag = sdt.getSdtPr().getTag();

		}

		// log.warn("Parsed SDT ID " + id);

	}

	public ElementMLFactory getElementMLFactory() {
		return factory;
	}

	public String getPlutextId() {
		return sdtWrapper.getPlutextId();
	}

	// public void setId(Id id) {
	// sdtWrapper.setId(id);
	// // this.id = id;
	// }

	// public xxxTag getVersion() {
	// return sdtWrapper.getVersionNumber();
	// }

	// public void setVersion(Tag tag) {
	// //this.tag = tag;
	// sdtWrapper.setVersionNumber(versionNumber);
	// }

	public Tag getTag() {
		return sdtWrapper.getTag();
	}

	// Has this transform been applied to the document yet?
	Boolean applied = false;

	public Boolean getApplied() {
		return applied;
	}

	public void setApplied(Boolean applied) {
		this.applied = applied;
	}

	// Is this transform something which came from this
	// plutext client? (If it is, we can always apply it without worrying
	// about conflicts)
	boolean local = false;

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	// The ID of the transformation.
	protected long sequenceNumber = 0;

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	protected long changesetNumber = 0;

	public long getChangesetNumber() {
		return changesetNumber;
	}

	public void setChangesetNumber(long number) {
		this.changesetNumber = number;
	}

	/* Code to apply the transform */
	public abstract long apply(Mediator mediator, HashMap<String, StateChunk> stateChunks);

	public abstract String markupChanges(String original, Changeset changeset);

	public org.docx4j.wml.SdtBlock getMarkedUpSdt() {
		return this.markedUpSdt;
	}

	protected void updateRefreshOffsets(Mediator mediator, int start, int end) {
		int offset = mediator.getUpdateStartOffset();
		offset = Math.min(offset, start);
		mediator.setUpdateStartOffset(offset);

		offset = mediator.getUpdateEndOffset();
		offset = Math.max(offset, end);
		mediator.setUpdateEndOffset(offset);
	}

}// TransformAbstract class

