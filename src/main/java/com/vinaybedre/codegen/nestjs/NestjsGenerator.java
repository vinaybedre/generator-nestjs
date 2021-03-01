package com.vinaybedre.codegen.nestjs;

import com.google.common.collect.Maps;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.*;
import io.swagger.models.properties.*;
import org.openapitools.codegen.utils.ModelUtils;

import java.util.*;
import java.io.File;
import java.util.stream.Collectors;

public class NestjsGenerator extends DefaultCodegen implements CodegenConfig {

  // source folder where to write the files
  protected String sourceFolder = "src";
  protected String apiVersion = "1.0.0";

  /**
   * Configures the type of generator.
   * 
   * @return  the CodegenType for this generator
   * @see     org.openapitools.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.SERVER;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -g flag.
   * 
   * @return the friendly name for the generator
   */
  public String getName() {
    return "nestjs";
  }

  /**
   * Provides an opportunity to inspect and modify operation data before the code is generated.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {

    // to try debugging your code generator:
    // set a break point on the next line.
    // then debug the JUnit test called LaunchGeneratorInDebugger

    Map<String, Object> results = super.postProcessOperationsWithModels(objs, allModels);
    System.out.println(results.keySet());

    Map<String, Object> ops = (Map<String, Object>)results.get("operations");
    ArrayList<CodegenOperation> opList = (ArrayList<CodegenOperation>)ops.get("operation");

    // iterate over the operation and perhaps modify something
    for(CodegenOperation co : opList){
      // example:
      co.httpMethod = co.httpMethod.substring(0,1).toUpperCase()+co.httpMethod.substring(1).toLowerCase();
      co.path = co.path.replaceAll("}","");
      co.path = co.path.replaceAll("\\{",":");
      for (CodegenParameter allParam : co.allParams) {
        if(allParam.dataType !=null && allParam.dataType.equalsIgnoreCase("number")) {
          allParam.isNumeric = true;
        }
        if(allParam.defaultValue != null && !allParam.defaultValue.equalsIgnoreCase("null")){
          allParam.vendorExtensions.put("isDefaultValueNull", true);
        }
      }
    }

    return results;
  }

  @Override
  public Map<String, Object> postProcessModels(Map<String, Object> objs) {
    List<Object> models = (List<Object>) postProcessModelsEnum(objs).get("models");
    for (Object _mo : models) {
      Map<String, Object> mo = (Map<String, Object>) _mo;
      CodegenModel cm = (CodegenModel) mo.get("model");
      for (CodegenProperty var : cm.vars) {
        if(var.defaultValue == null || var.defaultValue.equalsIgnoreCase("null")) {
          cm.vendorExtensions.put("isDefaultValueNull", true);
        }
      }

      cm.imports.remove("array");
      cm.imports.remove("Array");
      cm.imports.remove("integer");
      cm.imports.remove("set");
      cm.imports.remove("map");
      cm.imports.remove("file");
      cm.imports = new TreeSet(cm.imports);
      // name enum with model name, e.g. StatusEnum => Pet.StatusEnum
      for (CodegenProperty var : cm.vars) {
        if (Boolean.TRUE.equals(var.isEnum)) {
          var.datatypeWithEnum = var.datatypeWithEnum.replace(var.enumName, cm.classname + "." + var.enumName);
        }
      }
      if (cm.parent != null) {
        for (CodegenProperty var : cm.allVars) {
          if (Boolean.TRUE.equals(var.isEnum)) {
            var.datatypeWithEnum = var.datatypeWithEnum
                    .replace(var.enumName, cm.classname + "." + var.enumName);
          }
        }
      }
    }

    return objs;
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   * 
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a nestjs client library.";
  }

  public NestjsGenerator() {
    super();

    // set the output folder here
    outputFolder = "generated-code/nestjs";

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    modelTemplateFiles.put(
      "model.mustache", // the template to use
      ".ts");       // the extension for each file to write

    /**
     * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
     * as with models, add multiple entries with different extensions for multiple files per
     * class
     */
    apiTemplateFiles.put(
      "api.mustache",   // the template to use
      ".ts");       // the extension for each file to write

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "nestjs";

    /**
     * Api Package.  Optional, if needed, this can be used in templates
     */
    apiPackage = "api";

    /**
     * Model Package.  Optional, if needed, this can be used in templates
     */
    modelPackage = "model";

    /**
     * Reserved words.  Override this with reserved words specific to your language
     */
    reservedWords.addAll(Arrays.asList(
            // local variable names used in API methods (endpoints)
            "varLocalPath", "queryParameters", "headerParams", "formParams", "useFormData", "varLocalDeferred",
            "requestOptions",
            // Typescript reserved words
            "abstract", "await", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else", "enum", "export", "extends", "false", "final", "finally", "float", "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield"));

    languageSpecificPrimitives = new HashSet<>(Arrays.asList(
            "string",
            "String",
            "boolean",
            "Boolean",
            "Double",
            "Integer",
            "Long",
            "Float",
            "Object",
            "Array",
            "ReadonlyArray",
            "Date",
            "number",
            "any",
            "File",
            "Error",
            "Map",
            "object",
            "Set"
    ));

    instantiationTypes.put("array", "Array");

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("apiVersion", apiVersion);

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    supportingFiles.add(new SupportingFile("tsconfig.mustache",   // the input template or file
      "",                                                       // the destination folder, relative `outputFolder`
      "tsconfig.json")                                          // the output file
    );

    /**
     * Language Specific Primitives.  These types will not trigger imports by
     * the client generator
     */
    languageSpecificPrimitives = new HashSet<String>(
      Arrays.asList(
        "string",      // replace these with your types
        "number", "boolean","Object","object")
    );

    instantiationTypes.put("array", "Array");

    typeMapping = new HashMap<String, String>();
    typeMapping.put("Set", "Set");
    typeMapping.put("set", "Set");
    typeMapping.put("Array", "Array");
    typeMapping.put("array", "Array");
    typeMapping.put("boolean", "boolean");
    typeMapping.put("string", "string");
    typeMapping.put("int", "number");
    typeMapping.put("float", "number");
    typeMapping.put("number", "number");
    typeMapping.put("long", "number");
    typeMapping.put("short", "number");
    typeMapping.put("char", "string");
    typeMapping.put("double", "number");
    typeMapping.put("object", "object");
    typeMapping.put("integer", "number");
    typeMapping.put("Map", "any");
    typeMapping.put("map", "any");
    typeMapping.put("date", "string");
    typeMapping.put("DateTime", "string");
    typeMapping.put("binary", "any");
    typeMapping.put("File", "any");
    typeMapping.put("file", "any");
    typeMapping.put("ByteArray", "string");
    typeMapping.put("UUID", "string");
    typeMapping.put("URI", "string");
    typeMapping.put("Error", "Error");
    typeMapping.put("AnyType", "any");
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reserved words
   * 
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;  // add an underscore to the name
  }

  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
  }

  /**
   * Location to write api files.  You can use the apiPackage() as defined when the class is
   * instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
  }

  /**
   * override with any special text escaping logic to handle unsafe
   * characters so as to avoid code injection
   *
   * @param input String to be cleaned up
   * @return string with unsafe characters removed or escaped
   */
  @Override
  public String escapeUnsafeCharacters(String input) {
    //TODO: check that this logic is safe to escape unsafe characters to avoid code injection
    return input;
  }

  /**
   * Escape single and/or double quote to avoid code injection
   *
   * @param input String to be cleaned up
   * @return string with quotation mark removed or escaped
   */
  public String escapeQuotationMark(String input) {
    //TODO: check that this logic is safe to escape quotation mark to avoid code injection
    return input.replace("\"", "\\\"");
  }

  @Override
  public String getTypeDeclaration(Schema p) {
    if (ModelUtils.isArraySchema(p)) {
      Schema<?> items = getSchemaItems((ArraySchema) p);
      return getTypeDeclaration(ModelUtils.unaliasSchema(this.openAPI, items)) + "[]";
    } else if (ModelUtils.isMapSchema(p)) {
      Schema<?> inner = getSchemaAdditionalProperties(p);
      String nullSafeSuffix = "";//getNullSafeAdditionalProps() ? " | undefined" : "";
      return "{ [key: string]: " + getTypeDeclaration(ModelUtils.unaliasSchema(this.openAPI, inner)) + nullSafeSuffix  + "; }";
    } else if (ModelUtils.isFileSchema(p)) {
      return "any";
    } else if (ModelUtils.isBinarySchema(p)) {
      return "any";
    }
    return super.getTypeDeclaration(p);
  }

}