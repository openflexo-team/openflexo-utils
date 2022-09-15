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
 * @param <N>
 *            Type of AST node
 * @param <T>
 *            General type of pretty-printable object
 */
public class ConditionalContents<N, T> extends PrettyPrintableContents<N, T> {

	private static final Logger logger = Logger.getLogger(ConditionalContents.class.getPackage().getName());

	private final Supplier<Boolean> conditionSupplier;

	private PrettyPrintableContents<N, T> thenContents;
	private PrettyPrintableContents<N, T> elseContents;

	private boolean isFinal = false;

	/**
	 * Build a new {@link ConditionalContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public ConditionalContents(P2PPNode<N, T> node, Supplier<Boolean> conditionSupplier) {
		super(node);
		this.conditionSupplier = conditionSupplier;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public PrettyPrintableContents<N, T> getThenContents() {
		return thenContents;
	}

	public void setThenContents(PrettyPrintableContents<N, T> thenContents) {
		this.thenContents = thenContents;
	}

	public PrettyPrintableContents<N, T> getElseContents() {
		return elseContents;
	}

	public void setElseContents(PrettyPrintableContents<N, T> elseContents) {
		this.elseContents = elseContents;
	}

	public ConditionalContents<N, T> thenAppend(PrettyPrintableContents<N, T> contents, RawSourceFragment fragment) {
		if (thenContents == null) {
			if (fragment == null) {
				fragment = getNode().getDefaultInsertionPoint() != null ? getNode().getDefaultInsertionPoint().getOuterType()
						.makeFragment(getNode().getDefaultInsertionPoint(), getNode().getDefaultInsertionPoint()) : null;
			}
			contents.setFragment(fragment);
			thenContents = contents;
			if (fragment != null) {
				getNode().setDefaultInsertionPoint(fragment.getEndPosition());
			}
		}
		else if (thenContents instanceof SequentialContents) {
			((SequentialContents<N, T>) thenContents).append(contents, fragment);
		}
		else {
			SequentialContents<N, T> sequentialContents = new SequentialContents<>(getNode());
			sequentialContents.append(thenContents, thenContents.getFragment());
			sequentialContents.append(contents, fragment);
			thenContents = sequentialContents;
		}
		return this;
	}

	public ConditionalContents<N, T> thenAppend(ChildContents<N, T, ?, ?> contents) {
		return thenAppend(contents, null);
	}

	public ConditionalContents<N, T> thenAppend(ChildrenContents<N, T, ?, ?> contents) {
		return thenAppend(contents, null);
	}

	public ConditionalContents<N, T> elseAppend(PrettyPrintableContents<N, T> contents, RawSourceFragment fragment) {
		if (elseContents == null) {
			if (fragment == null) {
				fragment = getNode().getDefaultInsertionPoint() != null ? getNode().getDefaultInsertionPoint().getOuterType()
						.makeFragment(getNode().getDefaultInsertionPoint(), getNode().getDefaultInsertionPoint()) : null;
			}
			contents.setFragment(fragment);
			elseContents = contents;
			if (fragment != null) {
				getNode().setDefaultInsertionPoint(fragment.getEndPosition());
			}
		}
		else if (elseContents instanceof SequentialContents) {
			((SequentialContents<N, T>) elseContents).append(contents, fragment);
		}
		else {
			SequentialContents<N, T> sequentialContents = new SequentialContents<N, T>(getNode());
			sequentialContents.append(elseContents, elseContents.getFragment());
			sequentialContents.append(contents, fragment);
			elseContents = sequentialContents;
		}
		return this;
	}

	public ConditionalContents<N, T> elseAppend(ChildContents<N, T, ?, ?> contents) {
		return elseAppend(contents, null);
	}

	public ConditionalContents<N, T> elseAppend(ChildrenContents<N, T, ?, ?> contents) {
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

		super.updatePrettyPrint(derivedRawSource, context);

		if (conditionSupplier.get()) {
			// If initial contents was parsed with ELSE clause, remove that text
			if (!isFinal() && getElseContents() != null) {
				if (getElseContents().getFragment() != null && getElseContents().getFragment().getLength() > 0) {
					derivedRawSource.replace(getElseContents().getFragment(), "");
				}
			}

			if (getThenContents() != null) {
				getThenContents().updatePrettyPrint(derivedRawSource, context);
			}
			else {
				logger.warning("Unexpected null THEN contents");
			}
		}
		else {
			// If initial contents was parsed with THEN clause, remove that text
			if (!isFinal() && getThenContents() != null) {
				if (getThenContents().getFragment() != null && getThenContents().getFragment().getLength() > 0) {
					derivedRawSource.replace(getThenContents().getFragment(), "");
					System.out.println("For " + conditionSupplier + " in " + getNode() + " object=" + getNode().getModelObject());
					System.out.println(
							"Remove fragment " + getThenContents().getFragment() + " in " + getThenContents().getFragment().getRawText());
				}
			}

			if (getElseContents() != null) {
				getElseContents().updatePrettyPrint(derivedRawSource, context);
			}
		}

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
