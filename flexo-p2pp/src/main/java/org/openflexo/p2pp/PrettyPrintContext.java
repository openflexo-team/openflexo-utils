package org.openflexo.p2pp;

public interface PrettyPrintContext {

	public PrettyPrintContext derive(int indentation);

	public String getResultingIndentation();

	public String indent(String stringToIndent);

}
