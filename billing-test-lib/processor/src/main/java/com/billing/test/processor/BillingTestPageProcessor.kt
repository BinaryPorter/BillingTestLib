package com.billing.test.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.billing.test.annotation.BillingTestPage
import com.google.devtools.ksp.symbol.KSFile

class BillingTestPageProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("com.billing.test.annotation.BillingTestPage")
        val entries = mutableListOf<PageEntry>()

        for (symbol in symbols) {
            if (symbol !is KSClassDeclaration) continue
            if (!symbol.validate()) continue

            val annotation = symbol.getAnnotationsByType(BillingTestPage::class).firstOrNull()
                ?: continue

            val className = symbol.qualifiedName?.asString() ?: continue

            val factoryClassName = if (annotation.intentFactory.isEmpty()) {
                null
            } else {
                annotation.intentFactory
            }

            entries.add(
                PageEntry(
                    name = annotation.name,
                    category = annotation.category,
                    description = annotation.description,
                    type = annotation.type.name,
                    activityClassName = className,
                    intentFactoryClassName = factoryClassName
                )
            )
        }

        if (entries.isNotEmpty()) {
            val originatingFiles = symbols
                .filterIsInstance<KSClassDeclaration>()
                .mapNotNull { it.containingFile }
                .toList()
            generateRegistry(entries, originatingFiles)
        }
        return emptyList()
    }

    private fun generateRegistry(entries: List<PageEntry>, originatingFiles: List<KSFile>) {
        val implClassName = "BillingTestPageRegistryImpl"
        val implQualifiedName = "com.billing.test.generated.$implClassName"
        val dependencies = Dependencies(false, *originatingFiles.toTypedArray())

        val file = codeGenerator.createNewFile(
            dependencies,
            "com.billing.test.generated",
            implClassName
        )

        val writer = file.bufferedWriter()
        writer.appendLine("package com.billing.test.generated")
        writer.appendLine()
        writer.appendLine("import com.billing.test.annotation.BillingPageType")
        writer.appendLine("import com.billing.test.annotation.BillingTestPageEntry")
        writer.appendLine("import com.billing.test.annotation.BillingTestPageRegistry")
        writer.appendLine()
        writer.appendLine("class $implClassName : BillingTestPageRegistry {")
        writer.appendLine()
        writer.appendLine("    override fun getPages(): List<BillingTestPageEntry> = listOf(")

        if (entries.isEmpty()) {
            writer.appendLine("    )")
        } else {
            entries.forEachIndexed { index, entry ->
                writer.appendLine("        BillingTestPageEntry(")
                writer.appendLine("            name = \"${escapeKotlin(entry.name)}\",")
                writer.appendLine("            type = BillingPageType.${entry.type},")
                writer.appendLine("            category = \"${escapeKotlin(entry.category)}\",")
                writer.appendLine("            description = \"${escapeKotlin(entry.description)}\",")
                writer.appendLine("            activityClassName = \"${entry.activityClassName}\",")
                val factory = entry.intentFactoryClassName
                if (factory != null) {
                    writer.appendLine("            intentFactoryClassName = \"$factory\"")
                } else {
                    writer.appendLine("            intentFactoryClassName = null")
                }
                if (index == entries.lastIndex) {
                    writer.appendLine("        )")
                } else {
                    writer.appendLine("        ),")
                }
            }
            writer.appendLine("    )")
        }

        writer.appendLine("}")
        writer.close()

        val servicesFile = codeGenerator.createNewFile(
            dependencies,
            "",
            "META-INF/services/com.billing.test.annotation.BillingTestPageRegistry",
            ""
        )
        servicesFile.write("$implQualifiedName\n".toByteArray())
        servicesFile.close()
    }

    private fun escapeKotlin(str: String): String {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    }

    private data class PageEntry(
        val name: String,
        val category: String,
        val description: String,
        val type: String,
        val activityClassName: String,
        val intentFactoryClassName: String?
    )
}

class BillingTestPageProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return BillingTestPageProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
