package com.mlreef.parsing

import com.mlreef.antlr.Python3BaseVisitor
import com.mlreef.antlr.Python3Lexer
import com.mlreef.antlr.Python3Parser
import com.mlreef.rest.DataProcessor
import com.mlreef.rest.EPFAnnotation
import com.mlreef.rest.MetricSchema
import com.mlreef.rest.ProcessorParameter
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.InputStream
import java.util.logging.Logger

object MLPython3AnnotationFactory {
    fun create(context: ParsingContext, nameContext: Python3Parser.Dotted_nameContext, arglistContext: Python3Parser.ArglistContext): EPFAnnotation {
        val name: String = nameContext.text
        val arguments = arglistContext.argument()
        val argMap = transformContextToMap(arguments)
        val argList = transformContextToOrderedList(arguments)
        return when (name) {
            // hardcoded name for MLReef annotations
            "parameter" -> ModelExtractor.createParameter(context, argMap, argList)
            "data_processor" -> ModelExtractor.createDataOperation(argMap)
            "metric" -> ModelExtractor.createMetric(argMap, argList)
            else -> throw IllegalArgumentException("Not supported Annotation: $name")
        }
    }

    private fun transformContextToMap(values: MutableList<Python3Parser.ArgumentContext>): HashMap<String, String> {
        val map: HashMap<String, String> = HashMap()
        values.forEach { argumentContext ->
            if (argumentContext.text.isNotBlank() && argumentContext.test().size > 1) {
                val tupleKey = argumentContext.test(0).text
                val tupleValue = argumentContext.test(1).text.replace("\"", "")
                map[tupleKey] = cleanValue(tupleValue)
            }
        }
        return map
    }

    private fun transformContextToOrderedList(values: MutableList<Python3Parser.ArgumentContext>): List<String> {
        val list: MutableList<String> = arrayListOf()
        values.forEach { argumentContext ->
            if (argumentContext.text.isNotBlank() && argumentContext.test().size > 0) {
                val singleValue = cleanValue(argumentContext.test(0).text)
                list.add(singleValue)
            }
        }
        return list
    }

    fun cleanValue(string: String): String {
        return string
            .replace("\"", "")
            .replace("'", "")
    }
}


class ParsingContext {
    var metricSchema: MetricSchema? = null
    var dataProcessor: DataProcessor? = null
    var countParameters: Int = 0
}

class MLPython3Parser : MLParser {
    override fun parse(inStream: InputStream): MLParseResult {
        val result = MLParseResult()
        val lexer = Python3Lexer(CharStreams.fromStream(inStream))
        val tokens = CommonTokenStream(lexer)
        val parser = Python3Parser(tokens)

        val stmt = parser.file_input()
        val visitor = MLVisitor(result)
        visitor.visit(stmt)

        result.validate()
        return result
    }

    class MLVisitor(private val result: MLParseResult) : Python3BaseVisitor<Any>() {
        private val logger: Logger = Logger.getLogger(javaClass.simpleName)

        override fun visitFuncdef(ctx: Python3Parser.FuncdefContext?): Any {
            if (ctx != null) {
                result.countFunctions += 1
            }
            return 0
        }

        // Annotations on Python are named Decorators
        // ==> Decorated context is equal to an annotation
        override fun visitDecorated(context: Python3Parser.DecoratedContext?): Any {
            if (context != null) {
                val decorators = context.decorators()
                val funcdef = context.funcdef()
                val name = funcdef.NAME()
                result.countFunctions += 1
                checkDecorators(decorators)
                logger.info("parsing decorated function: $name with ${decorators.text}")
            }
            return 0
        }

        private fun checkDecorators(decorators: Python3Parser.DecoratorsContext) {
            val context = ParsingContext()

            var foundMLAnnotation = false
            decorators.children
                .filterIsInstance<Python3Parser.DecoratorContext>()
                .forEach {
                    println("Annotation: ${it.text}")
                    try {
                        val children = it.children
                        //e.g. @parameter(parameters...)
                        //@parameters => Dotted_namedContext
                        //parameters.. => arglist
                        //children[0] = "@"
                        //children[1] = "parameter"
                        //children[2] = "("
                        //children[3] = parameters...
                        //children[4] = "4"
                        val annotationType = children[1] as Python3Parser.Dotted_nameContext
                        val annotationValues = children[3] as Python3Parser.ArglistContext
                        val mlAnnotation = MLPython3AnnotationFactory.create(context, annotationType, annotationValues)
                        foundMLAnnotation = true
                        result.mlAnnotations += mlAnnotation
                        if (mlAnnotation is ProcessorParameter) {
                            result.countMLFunctionParameters += 1
                        } else if (mlAnnotation is DataProcessor) {
                            result.countMLDataProcessor += 1
                            context.dataProcessor = mlAnnotation
                        } else if (mlAnnotation is MetricSchema) {
                            result.countMLMetricSchema += 1
                            context.metricSchema = mlAnnotation
                        } else {
                            logger.warning("Could not handle:$mlAnnotation")
                        }
                    } catch (error: Exception) {
                        logger.warning(error.message)
                    }
                }
            if (foundMLAnnotation) {
                result.countMLDecoratedFunctions += 1
            }
        }
    }
}

