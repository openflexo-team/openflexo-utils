/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.p2pp;

import java.util.function.Supplier;
import java.util.logging.Logger;

import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * A conditional PrettyPrintableContents
 * 
 * This is a control-structure defining alternatives for pretty-printing for a {@link P2PPNode}
 * 
 * @author sylvain
 *
 */
public class ConditionalContents extends PrettyPrintableContents {

	private static final Logger logger = Logger.getLogger(ConditionalContents.class.getPackage().getName());

	private final Supplier<Boolean> conditionSupplier;

	private P2PPNode<?, ?> node;

	private PrettyPrintableContents thenContents;
	private PrettyPrintableContents elseContents;

	/**
	 * Build a new {@link ConditionalContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public ConditionalContents(Supplier<Boolean> conditionSupplier, P2PPNode<?, ?> node) {
		super();
		this.node = node;
		this.conditionSupplier = conditionSupplier;
		// setFragment(fragment);
	}

	public PrettyPrintableContents getThenContents() {
		return thenContents;
	}

	public void setThenContents(PrettyPrintableContents thenContents) {
		this.thenContents = thenContents;
	}

	public PrettyPrintableContents getElseContents() {
		return elseContents;
	}

	public void setElseContents(PrettyPrintableContents elseContents) {
		this.elseContents = elseContents;
	}

	public ConditionalContents thenAppend(PrettyPrintableContents contents, RawSourceFragment fragment) {
		if (thenContents == null) {
			if (fragment == null) {
				fragment = node.getDefaultInsertionPoint() != null ? node.getDefaultInsertionPoint().getOuterType()
						.makeFragment(node.getDefaultInsertionPoint(), node.getDefaultInsertionPoint()) : null;
			}
			contents.setFragment(fragment);
			thenContents = contents;
			if (fragment != null) {
				node.setDefaultInsertionPoint(fragment.getEndPosition());
			}
		}
		else if (thenContents instanceof SequentialContents) {
			((SequentialContents) thenContents).append(contents, fragment);
		}
		else {
			SequentialContents sequentialContents = new SequentialContents(node);
			sequentialContents.append(thenContents, thenContents.getFragment());
			sequentialContents.append(contents, fragment);
			thenContents = sequentialContents;
		}
		return this;
	}

	public ConditionalContents thenAppend(ChildContents<?> contents) {
		return thenAppend(contents, null);
	}

	public ConditionalContents thenAppend(ChildrenContents<?> contents) {
		return thenAppend(contents, null);
	}

	public ConditionalContents elseAppend(PrettyPrintableContents contents, RawSourceFragment fragment) {
		if (elseContents == null) {
			if (fragment == null) {
				fragment = node.getDefaultInsertionPoint() != null ? node.getDefaultInsertionPoint().getOuterType()
						.makeFragment(node.getDefaultInsertionPoint(), node.getDefaultInsertionPoint()) : null;
			}
			contents.setFragment(fragment);
			elseContents = contents;
			if (fragment != null) {
				node.setDefaultInsertionPoint(fragment.getEndPosition());
			}
		}
		else if (elseContents instanceof SequentialContents) {
			((SequentialContents) elseContents).append(contents, fragment);
		}
		else {
			SequentialContents sequentialContents = new SequentialContents(node);
			sequentialContents.append(elseContents, elseContents.getFragment());
			sequentialContents.append(contents, fragment);
			elseContents = sequentialContents;
		}
		return this;
	}

	public ConditionalContents elseAppend(ChildContents<?> contents) {
		return elseAppend(contents, null);
	}

	public ConditionalContents elseAppend(ChildrenContents<?> contents) {
		return elseAppend(contents, null);
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {

		if (conditionSupplier.get()) {
			if (getThenContents() != null) {
				return getThenContents().getNormalizedPrettyPrint(context);
			}
			else {
				return "<???>";
			}
		}
		else {
			if (getElseContents() != null) {
				return getElseContents().getNormalizedPrettyPrint(context);
			}
			else {
				return "";
			}
		}

	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {

		if (conditionSupplier.get()) {
			if (getThenContents() != null) {
				getThenContents().updatePrettyPrint(derivedRawSource, context);
			}
			else {
				logger.warning("Unexpected null THEN contents");
			}
		}
		else {
			if (getElseContents() != null) {
				getElseContents().updatePrettyPrint(derivedRawSource, context);
			}
		}

		// System.out.println("> Pour DynamicContents, faudrait passer " + getFragment() + " a " + stringRepresentationSupplier.get());
		// derivedRawSource.replace(getFragment(), stringRepresentationSupplier.get());
	}

	@Override
	public void initializePrettyPrint(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		if (getThenContents() != null) {
			getThenContents().initializePrettyPrint(rootNode, context);
		}
		if (getElseContents() != null) {
			getElseContents().initializePrettyPrint(rootNode, context);
		}
	}

}
