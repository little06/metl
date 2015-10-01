/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.metl.core.runtime.component.helpers;

import java.util.ArrayList;

import org.jumpmind.metl.core.runtime.EntityData;
import org.jumpmind.metl.core.runtime.component.AbstractComponentRuntimeTest;
import org.jumpmind.metl.core.util.NameValue;

public class PayloadTestHelper {

	public static ArrayList<EntityData> createPayloadWithEntityData(String nvName, Object nvValue) {
		ArrayList<EntityData> payload = new ArrayList<EntityData>();
		addEntityDataToPayload(payload, nvName, nvValue);
		return payload;
	}
	
	public static void addEntityDataToPayload(ArrayList<EntityData> payload, String nvName, Object nvValue) {
		NameValue nv = new NameValue(nvName, nvValue);
		EntityData entity = new EntityData(nv);
		payload.add(entity);
	}
	
	public static ArrayList<EntityData> createPayloadWithMultipleEntityData() {
		ArrayList<EntityData> payload = new ArrayList<EntityData>();
		
		EntityData entity = new EntityData();
		entity.put(AbstractComponentRuntimeTest.MODEL_ATTR_ID_1, AbstractComponentRuntimeTest.MODEL_ATTR_NAME_1);
		entity.put(AbstractComponentRuntimeTest.MODEL_ATTR_ID_2, AbstractComponentRuntimeTest.MODEL_ATTR_NAME_2);
		entity.put(AbstractComponentRuntimeTest.MODEL_ATTR_ID_3, AbstractComponentRuntimeTest.MODEL_ATTR_NAME_3);
		payload.add(entity);
		return payload;
	}
	
}