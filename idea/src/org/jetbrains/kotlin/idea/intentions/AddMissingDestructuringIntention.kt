/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.intentions

import com.intellij.openapi.editor.Editor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.KotlinBundle
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.inspections.IncompleteDestructuringQuickfix
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration

class AddMissingDestructuringIntention : SelfTargetingIntention<KtDestructuringDeclaration>(
    KtDestructuringDeclaration::class.java,
    KotlinBundle.lazyMessage("add.missing.component")
) {
    override fun isApplicableTo(element: KtDestructuringDeclaration, caretOffset: Int): Boolean {
        val entriesCount = element.entries.size

        val classDescriptor = element.classDescriptor() ?: return false
        if (!classDescriptor.isData) return false

        val primaryParameters = classDescriptor.primaryParameters() ?: return false
        return primaryParameters.size > entriesCount
    }

    override fun applyTo(element: KtDestructuringDeclaration, editor: Editor?) {
        IncompleteDestructuringQuickfix.addMissingEntries(element)
    }

    private fun KtDestructuringDeclaration.classDescriptor(): ClassDescriptor? {
        val type = initializer?.let { it.analyze().getType(it) } ?: return null
        return type.constructor.declarationDescriptor as? ClassDescriptor ?: return null
    }

    private fun ClassDescriptor.primaryParameters() = constructors.firstOrNull { it.isPrimary }?.valueParameters
}