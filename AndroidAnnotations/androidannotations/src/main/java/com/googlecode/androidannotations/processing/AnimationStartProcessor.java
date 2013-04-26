/**
 * Copyright (C) 2010-2012 eBusiness Information, Excilys Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.androidannotations.processing;

import static com.sun.codemodel.JExpr._new;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import com.googlecode.androidannotations.annotations.AnimationStart;
import com.googlecode.androidannotations.helper.APTCodeModelHelper;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

public class AnimationStartProcessor implements DecoratingElementProcessor {

	private final APTCodeModelHelper helper = new APTCodeModelHelper();

	public AnimationStartProcessor() {
	}

	@Override
	public Class<? extends Annotation> getTarget() {
		return AnimationStart.class;
	}

	@Override
	public void process(Element element, JCodeModel codeModel, EBeanHolder holder) throws JClassAlreadyExistsException {

		ExecutableElement executableElement = (ExecutableElement) element;

		JMethod delegatingMethod = helper.overrideAnnotatedMethod(executableElement, holder);

		JDefinedClass innerRunnableClass = helper.createDelegatingAnonymousRunnableClass(holder, delegatingMethod);

		{
			// Execute Runnable

			if (holder.handler == null) {
				JClass handlerClass = holder.classes().HANDLER;
				holder.handler = holder.generatedClass.field(JMod.PRIVATE, handlerClass, "handler_", JExpr._new(handlerClass));
			}

			JDefinedClass outerRunnableClass = createOuterRunnableClass(holder, innerRunnableClass);

			JClass viewCompatClass = codeModel.ref("android.support.v4.view.ViewCompat");
			JExpression viewExpr = JExpr.direct("getWindow().getDecorView()");
			delegatingMethod.body().staticInvoke(viewCompatClass, "postOnAnimation").arg(viewExpr).arg(_new(outerRunnableClass));

		}
	}

	private JDefinedClass createOuterRunnableClass(EBeanHolder holder, JDefinedClass inner) {
		JCodeModel codeModel = holder.codeModel();
		// Classes classes = holder.classes();

		JDefinedClass anonymousRunnableClass;

		anonymousRunnableClass = codeModel.anonymousClass(Runnable.class);

		JMethod runMethod = anonymousRunnableClass.method(JMod.PUBLIC, codeModel.VOID, "run");
		runMethod.annotate(Override.class);

		JBlock runMethodBody = runMethod.body();

		JClass viewCompatClass = codeModel.ref("android.support.v4.view.ViewCompat");
		JExpression viewExpr = JExpr.direct("getWindow().getDecorView()");
		runMethodBody.staticInvoke(viewCompatClass, "postOnAnimation").arg(viewExpr).arg(_new(inner));

		return anonymousRunnableClass;
	}
}
