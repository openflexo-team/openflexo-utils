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

package org.plutext.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.plutext.Context;
import org.plutext.client.diffengine.DiffEngine;
import org.plutext.client.diffengine.DiffEngineLevel;
import org.plutext.client.diffengine.DiffResultSpan;
import org.plutext.client.diffengine.IDiffList;
import org.plutext.server.transitions.Transitions;

public class Skeleton implements IDiffList<TextLine> {
	private static Logger log = LoggerFactory.getLogger(Skeleton.class);

	private static void TextDiff(Skeleton source, Skeleton dest) {

		try {
			double time = 0;
			DiffEngine de = new DiffEngine();
			time = de.processDiff(source, dest, DiffEngineLevel.MEDIUM);

			ArrayList<DiffResultSpan> rep = de.getDiffLines();

			// log.Debug(de.Results(source, dest, rep));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Ordered list of ribs
	private ArrayList<TextLine> ribs = new ArrayList<TextLine>();

    /* We also need to know the @version of each rib:
     * 
     *      <ns3:rib ns3:version="2" ns3:id="1773260365">
     * 
     * so, client-side, we can detect whether the server
     * copy is newer.
     */
    private Map<String, Long> versions = new HashMap<String, Long>();

	public Skeleton() {
		;// do nothing
	}

    // Constructor - make object from string
    public Skeleton(String skeletonStr) {
    	this(skeletonStr, -1);
    }
    
	public Skeleton(String skeletonStr, long tSequenceNumberHighestFetched) {
        /*
         * <dst:transitions>
         *   <dst:ribs>
         *      <dst:rib id="54989358" />
         *      <dst:rib id="1447653797" />
         *        :
         * </dst:transitions>
         * 
         * */
        try {
			Unmarshaller u = Context.jcTransitions.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
			org.plutext.server.transitions.Transitions transitions = 
				(org.plutext.server.transitions.Transitions) u
					.unmarshal(new javax.xml.transform.stream.StreamSource(
							new java.io.StringReader(skeletonStr)));
			init(transitions, tSequenceNumberHighestFetched);
		} catch (JAXBException exc) {
			exc.printStackTrace(); //should not happen
		}
	}
	
	public Skeleton(Transitions t) {
		/*
		 * <dst:transitions> <dst:ribs> <dst:rib id="54989358" /> <dst:rib
		 * id="1447653797" /> : </dst:transitions>
		 * 
		 */
		init(t, -1);
	}
	
	public Skeleton(Transitions t, long tSequenceNumberHighestFetched) {
		init(t, tSequenceNumberHighestFetched);
	}
	
	public boolean init(Transitions t, long tSequenceNumberHighestFetched) {
        // if tSequenceNumberHighestFetched > -1, 
        // we return prematurely, with value false,
        // if an @snum > tSequenceNumberHighestFetched is found

		for (Transitions.Ribs.Rib r: t.getRibs().getRib()) {
			
			
            // We don't want our fake Sdt for document level sectPr
            // to be diffed 
            if ( Long.toString(r.getId()).equals(Mediator.SECTPR_MAGIC_ID) ) {
                continue;
            }
			
			
            if (tSequenceNumberHighestFetched > -1) {
            	for (Transitions.Ribs.Rib.T ribT: r.getT()) {
            		String op = ribT.getOp();

//                    if (op.equals("insert")
//                        || op.equals("move")
//                        || op.equals("delete")) // a structural transform
//                    {

                    	long thisSequenceNumber = ribT.getSnum();

                        if (thisSequenceNumber > tSequenceNumberHighestFetched)
                        {
                            log.debug("found transform snum " + thisSequenceNumber + " > " + tSequenceNumberHighestFetched);
                            //hasStructuralTransform = true;
                            return false;
                        }
//                    }
                }
            }

			if (r.isDeleted() != null && r.isDeleted()) {
                log.debug("Rib " + r.getId() + " deleted, so ignoring.");				
			} else {
                log.debug("Added Rib " + r.getId());
				ribs.add(new TextLine(Long.toString(r.getId())));
				
				versions.put(Long.toString(r.getId()), Long.valueOf(r.getVersion()));
			}
			
		}// for (r) loop
		return true;
	}// init()
	
	
	private boolean hasStructuralTransform = false;
	public boolean hasStructuralTransform() {
		return hasStructuralTransform;
	}
	
	public ArrayList<TextLine> getRibs() {
		return ribs;
	}

	public void setRibs(ArrayList<TextLine> ribs) {
		this.ribs = ribs;
	}

	public int count() {
		return ribs.size();
	}

	public Comparable<TextLine> getByIndex(int index) {
		return ribs.get(index);
	}
	
	public boolean removeRib(TextLine rib) {
//    	debugRibs();
		return ribs.remove(rib);
	}

    public Long getVersion(String ribId) {
        return versions.get(ribId);
    }

//    private void debugRibs() {    	
//    	for (TextLine rib : ribs) {    		
//    		log.info("'" + rib.getLine() + "'");
//    	}
//    }

}// Skeleton class
























