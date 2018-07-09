package com.synertone.butterknife_compiler;

import com.google.auto.service.AutoService;
import com.synertone.butterknife_annotation.BindView;
import com.synertone.butterknife_annotation.OnClick;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class ButterknifeProcess extends AbstractProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types=new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("-------------process--------------");
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //区分 key为Activity全类名
        Map<String,List<VariableElement>> cacheMap=new HashMap<>();
        Iterator<? extends Element> iterator = elementSet.iterator();
        while (iterator.hasNext()){
            Element next = iterator.next();
            VariableElement variableElement= (VariableElement) next;
            String activityName=getActivityName(variableElement);
            List<VariableElement> variableElementsList = cacheMap.get(activityName);
            if(variableElementsList==null){
                variableElementsList=new ArrayList<>();
                cacheMap.put(activityName,variableElementsList);
            }
            variableElementsList.add(variableElement);
        }


        Set<? extends Element> clickElementSet = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        //区分 key为Activity全类名
        Map<String,List<ExecutableElement>> cacheClickMap=new HashMap<>();
        Iterator<? extends Element> iteratorClick = clickElementSet.iterator();
        while (iteratorClick.hasNext()){
            Element next = iteratorClick.next();
            ExecutableElement executableElement= (ExecutableElement) next;
            String activityName=getActivityName(executableElement);
            List<ExecutableElement> executableElementsList = cacheClickMap.get(activityName);
            if(executableElementsList==null){
                executableElementsList=new ArrayList<>();
                cacheClickMap.put(activityName,executableElementsList);
            }
            executableElementsList.add(executableElement);
        }


        //为每一个activity生成 Java类
        Set<Map.Entry<String, List<VariableElement>>> entries = cacheMap.entrySet();
        Iterator<Map.Entry<String, List<VariableElement>>> entryIterator = entries.iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String, List<VariableElement>> next = entryIterator.next();
            String activityName = next.getKey();
            List<VariableElement> elementList = next.getValue();
            List<ExecutableElement> executableElements = cacheClickMap.get(activityName);
            generateJava(activityName, elementList,executableElements);
        }
        return false;
    }

    private void generateJava(String activityName, List<VariableElement> elementList,List<ExecutableElement> executableElements) {
        Filer filer = processingEnv.getFiler();
        String simpleActivityName=elementList.get(0).getEnclosingElement().getSimpleName().toString()+"$ViewBinder";
        try {
            JavaFileObject sourceFile = filer.createSourceFile(activityName+"$ViewBinder");
            Writer writer = sourceFile.openWriter();
            writer.write("package "+getPackageName(elementList.get(0))+";");
            writer.write("\n");
            writer.write("import com.synertone.butterknife.ViewBinder;");
            if(executableElements!=null){
                writer.write("import com.synertone.butterknife.ViewBinderClick;");
            }
            writer.write("\n");
            writer.write("import android.view.View;");
            writer.write("\n");
            if(executableElements!=null){
                writer.write("public class "+simpleActivityName+" implements ViewBinder<"+activityName+">,ViewBinderClick<"+activityName+">{");
            }else{
            writer.write("public class "+simpleActivityName+" implements ViewBinder<"+activityName+">{");
            }

            writer.write("\n");
            writer.write("public void bind(final "+activityName+" target){");
            writer.write("\n");
            for (VariableElement variableElement:elementList){
                TypeMirror typeMirror = variableElement.asType();
                BindView bindView = variableElement.getAnnotation(BindView.class);
                int id = bindView.value();
                writer.write("target."+variableElement.getSimpleName()+"=("+typeMirror.toString()+")target.findViewById("+id+");");
                writer.write("\n");
            }
            writer.write("}");
            writer.write("\n");
            if(executableElements!=null){
                for(ExecutableElement executableElement:executableElements){
                    OnClick onClick = executableElement.getAnnotation(OnClick.class);
                    int[] value = onClick.value();
                    writer.write("public void onClick(final "+activityName+" target){");
                    writer.write("\n");
                    for(int i=0;i<value.length;i++){
                        writer.write("target.findViewById("+value[i]+")"+".setOnClickListener(new View.OnClickListener() {");
                        writer.write("\n");
                        writer.write(" public void onClick(View v) {");
                        writer.write("\n");
                        writer.write(" target.onClick(target.findViewById("+value[i]+"));");
                        writer.write("\n");
                        writer.write("}");
                        writer.write("\n");
                        writer.write("});");
                        writer.write("\n");
                    }

                    writer.write("}");
                }
            }

            writer.write("\n");
            writer.write("}");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getActivityName(Element variableElement) {
        String packageName=getPackageName(variableElement);
       TypeElement typeElement= (TypeElement) variableElement.getEnclosingElement();
        return packageName+"."+typeElement.getSimpleName().toString();
    }

    private String getPackageName(Element variableElement) {
        Element enclosingElement = variableElement.getEnclosingElement();
        TypeElement typeElement= (TypeElement) enclosingElement;
        Name qualifiedName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName();
        return qualifiedName.toString();
    }
}
