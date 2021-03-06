/*******************************************************************************
 * Copyright (C) 2010 Ansgar Gerlicher
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Stuttgart, Hochschule der Medien: http://www.mi.hdm-stuttgart.de/mmb/
 * Collaborative Editing Framework or XML:
 * http://sourceforge.net/projects/cefx/
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.hdm.cefx.concurrency.operations;

import de.hdm.cefx.concurrency.operations.ComplexOperation;
import de.hdm.cefx.concurrency.operations.ComplexOperationHandler;
import de.hdm.cefx.concurrency.operations.Operation;
import de.hdm.cefx.concurrency.operations.OperationHandler;

public class CollabDrawingHandler extends ComplexOperationHandler {

	public CollabDrawingHandler(OperationHandler nextHandler) {
		super(nextHandler);
	}

	@Override
	public void handleOperation(ComplexOperation complexOp) {
		// TODO handle collaborative drawing complex operation
		
	}

	@Override
	public void handleOperation(Operation op) {
		// TODO check which type of complex operation it is ...
		
	}

}
