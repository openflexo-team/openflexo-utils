/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexoutils, a component of the software infrastructure 
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

package org.openflexo.toolbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Properties are written sorted by key, whatever the Java version: dictionaries are versioned, and an unsorted file makes one added entry
 * look like a rewrite of the whole file.
 */
public class TestFlexoProperties {

	private static List<String> storedKeys(FlexoProperties properties) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		properties.store(out, "test");
		List<String> keys = new ArrayList<>();
		for (String line : new String(out.toByteArray(), StandardCharsets.ISO_8859_1).split("\n")) {
			if (!line.isEmpty() && !line.startsWith("#")) {
				keys.add(line.substring(0, line.indexOf('=')));
			}
		}
		return keys;
	}

	@Test
	public void testStoredSortedByKey() throws IOException {
		FlexoProperties properties = new FlexoProperties();
		for (String key : new String[] { "zebra", "alpha", "Beta", "delete", "newPoint", "CENTER", "ARC", "new", "spacing_x", "a" }) {
			properties.setProperty(key, key.toUpperCase());
		}
		List<String> keys = storedKeys(properties);
		assertEquals(10, keys.size());
		List<String> sorted = new ArrayList<>(keys);
		Collections.sort(sorted);
		assertEquals(sorted, keys);
	}

	@Test
	public void testAnAddedEntryDoesNotMoveTheOthers() throws IOException {
		FlexoProperties properties = new FlexoProperties();
		for (int i = 0; i < 200; i++) {
			properties.setProperty("key_" + i, "value " + i);
		}
		List<String> before = storedKeys(properties);
		properties.setProperty("added", "value");
		List<String> after = storedKeys(properties);
		List<String> withoutAdded = new ArrayList<>(after);
		assertTrue(withoutAdded.remove("added"));
		assertEquals(before, withoutAdded);
	}

	@Test
	public void testEntrySetUnchangedOutsideStore() {
		FlexoProperties properties = new FlexoProperties();
		properties.setProperty("b", "1");
		properties.setProperty("a", "2");
		assertEquals(2, properties.entrySet().size());
		assertFalse(properties.isStoring());
	}
}
