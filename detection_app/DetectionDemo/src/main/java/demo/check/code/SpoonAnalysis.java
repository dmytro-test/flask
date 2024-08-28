package demo.check.code;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpoonAnalysis {

    private final static Logger LOGGER = Logger.getLogger(SpoonAnalysis.class.getName());

    private final Launcher launcher;

    public SpoonAnalysis(Path sourceCodePath) {
        launcher = new Launcher();
        launcher.addInputResource(sourceCodePath.toString());
    }

    public Set<String> analyze() {
        CtModel ctModel = launcher.buildModel();
        List<CtMethod<?>> methods = ctModel.getElements(new TypeFilter<>(CtMethod.class));
        Set<String> allMethods = methods.stream().map(v ->
                String.format("<%s: %s %s>", v.getDeclaringType().getQualifiedName(), v.getType().getSimpleName(), v.getSignature()))
                .collect(Collectors.toCollection(HashSet::new));

       LOGGER.info("Total number of methods in source code: [" + allMethods.size() + "]");

        return allMethods;
    }
}
