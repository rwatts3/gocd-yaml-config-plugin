package cd.go.plugin.config.yaml.transforms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static cd.go.plugin.config.yaml.JSONUtils.addOptionalValue;
import static cd.go.plugin.config.yaml.YamlUtils.*;

public class MaterialTransform {

    public static final String JSON_MATERIAL_TYPE_FIELD = "type";
    public static final String JSON_MATERIAL_NAME_FIELD = "name";
    public static final String JSON_MATERIAL_AUTO_UPDATE_FIELD = "auto_update";
    public static final String JSON_MATERIAL_SHALLOW_CLONE_FIELD = "shallow_clone";

    public static final String YAML_MATERIAL_TYPE_FIELD = "type";
    public static final String YAML_MATERIAL_AUTO_UPDATE_FIELD = "auto_update";
    public static final String YAML_MATERIAL_SHALLOW_CLONE_FIELD = "shallow_clone";

    public static final String YAML_SHORT_KEYWORD_GIT = "git";
    //TODO others

    public static final String YAML_BLACKLIST_KEYWORD = "blacklist";
    private static final String YAML_SHORT_KEYWORD_DEPENDENCY = "pipeline";
    private static final String YAML_SHORT_KEYWORD_SCM_ID = "scm";
    private static final String YAML_SHORT_KEYWORD_PACKAGE_ID = "package";
    private static final String YAML_SHORT_KEYWORD_SVN = "svn";
    private static final String JSON_MATERIAL_CHECK_EXTERNALS_FIELD = "check_externals";
    private static final String YAML_MATERIAL_CHECK_EXTERNALS_FIELD = "check_externals";
    private static final String YAML_SHORT_KEYWORD_HG = "hg";

    private static final String YAML_SHORT_KEYWORD_PERFORCE = "p4";
    private static final String JSON_MATERIAL_USE_TICKETS_FIELD = "use_tickets";
    private static final String YAML_MATERIAL_USE_TICKETS_FIELD = "use_tickets";

    private final HashSet<String> yamlSpecialKeywords = new HashSet<String>();

    public MaterialTransform() {
        yamlSpecialKeywords.add(YAML_SHORT_KEYWORD_GIT);
        // TODO all other transforms
        yamlSpecialKeywords.add("type");
        yamlSpecialKeywords.add("auto_update");
        yamlSpecialKeywords.add("shallow_clone");
        yamlSpecialKeywords.add("blacklist");
        yamlSpecialKeywords.add("whitelist");
        yamlSpecialKeywords.add("scm_id");
        yamlSpecialKeywords.add("package_id");
        yamlSpecialKeywords.add("svn");
        yamlSpecialKeywords.add("check_externals");
        yamlSpecialKeywords.add("hg");
        yamlSpecialKeywords.add("p4");
        yamlSpecialKeywords.add("use_tickets");
        yamlSpecialKeywords.add(YAML_SHORT_KEYWORD_PACKAGE_ID);
        yamlSpecialKeywords.add(YAML_SHORT_KEYWORD_SCM_ID);
    }

    public JsonObject transform(Object maybeMaterial) {
        Map<String, Object> map = (Map<String, Object>) maybeMaterial;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            return transform(entry);
        }
        throw new RuntimeException("expected material hash to have 1 item");
    }

    public LinkedTreeMap<String, Object> inverseTransform(LinkedTreeMap<String, Object> material) {
        String materialName = (String) material.get(JSON_MATERIAL_NAME_FIELD);
        LinkedTreeMap<String, Object> inverseMaterial = new LinkedTreeMap<>();
        LinkedTreeMap<String, Object> materialdata = new LinkedTreeMap<>();

        String materialType = (String) material.get(JSON_MATERIAL_TYPE_FIELD);

        switch (materialType) {
            case "p4":
                materialdata.put(YAML_SHORT_KEYWORD_PERFORCE, material.get("port"));
                break;
            case "package":
                materialdata.put(YAML_SHORT_KEYWORD_PACKAGE_ID, material.get("package_id"));
                break;
            case "plugin":
                materialdata.put(YAML_SHORT_KEYWORD_SCM_ID, material.get("scm_id"));
                break;
            case "hg":
                materialdata.put(YAML_SHORT_KEYWORD_HG, material.get("url"));
                break;
            case "git":
                materialdata.put(YAML_SHORT_KEYWORD_GIT, material.get("url"));
                break;
            case "svn":
                materialdata.put(YAML_SHORT_KEYWORD_SVN, material.get("url"));
                break;
            case "configrepo":
                materialdata.put(YAML_MATERIAL_TYPE_FIELD, material.get(JSON_MATERIAL_TYPE_FIELD));
                break;
        }

        addOptionalValue(materialdata, material, "username", "username");
        addOptionalValue(materialdata, material, "password", "password");
        addOptionalValue(materialdata, material, JSON_MATERIAL_USE_TICKETS_FIELD, YAML_MATERIAL_USE_TICKETS_FIELD );
        addOptionalValue(materialdata, material, "view", "view" );

        if (material.containsKey("filter"))
            addInverseFilter(materialdata, (LinkedTreeMap<String, Object>) material.get("filter"));

        addOptionalValue(materialdata, material, JSON_MATERIAL_SHALLOW_CLONE_FIELD, YAML_MATERIAL_SHALLOW_CLONE_FIELD);
        addOptionalValue(materialdata, material, JSON_MATERIAL_CHECK_EXTERNALS_FIELD, YAML_MATERIAL_CHECK_EXTERNALS_FIELD);
        addOptionalValue(materialdata, material, JSON_MATERIAL_AUTO_UPDATE_FIELD, YAML_MATERIAL_AUTO_UPDATE_FIELD);


        // copy all other members
        for (Map.Entry<String, Object> materialProp : material.entrySet()) {
            if (yamlSpecialKeywords.contains(materialProp.getKey()))
                continue;
            if (materialProp.getValue() instanceof String)
                materialdata.put(materialProp.getKey(), (String) materialProp.getValue());
        }

        inverseMaterial.put(materialName, materialdata);
        return inverseMaterial;
    }

    public JsonObject transform(Map.Entry<String, Object> entry) {
        String materialName = entry.getKey();
        JsonObject material = new JsonObject();
        material.addProperty(JSON_MATERIAL_NAME_FIELD, materialName);
        Map<String, Object> materialMap = (Map<String, Object>) entry.getValue();
        addOptionalString(material, materialMap, JSON_MATERIAL_TYPE_FIELD, YAML_MATERIAL_TYPE_FIELD);
        addOptionalBoolean(material, materialMap, JSON_MATERIAL_AUTO_UPDATE_FIELD, YAML_MATERIAL_AUTO_UPDATE_FIELD);
        addOptionalBoolean(material, materialMap, JSON_MATERIAL_SHALLOW_CLONE_FIELD, YAML_MATERIAL_SHALLOW_CLONE_FIELD);
        addOptionalBoolean(material, materialMap, JSON_MATERIAL_CHECK_EXTERNALS_FIELD, YAML_MATERIAL_CHECK_EXTERNALS_FIELD);
        addOptionalBoolean(material, materialMap, JSON_MATERIAL_USE_TICKETS_FIELD, YAML_MATERIAL_USE_TICKETS_FIELD);
        if (materialMap.containsKey("blacklist"))
            addFilter(material, materialMap.get("blacklist"), "ignore");
        if (materialMap.containsKey("whitelist"))
            addFilter(material, materialMap.get("whitelist"), "whitelist");

        String git = getOptionalString(materialMap, YAML_SHORT_KEYWORD_GIT);
        if (git != null) {
            material.addProperty(JSON_MATERIAL_TYPE_FIELD, "git");
            material.addProperty("url", git);
        }
        String svn = getOptionalString(materialMap, YAML_SHORT_KEYWORD_SVN);
        if (svn != null) {
            material.addProperty(JSON_MATERIAL_TYPE_FIELD, "svn");
            material.addProperty("url", svn);
        }
        String hg = getOptionalString(materialMap, YAML_SHORT_KEYWORD_HG);
        if (hg != null) {
            material.addProperty(JSON_MATERIAL_TYPE_FIELD, "hg");
            material.addProperty("url", hg);
        }
        String dependency = getOptionalString(materialMap, YAML_SHORT_KEYWORD_DEPENDENCY);
        if (dependency != null) {
            material.addProperty(JSON_MATERIAL_TYPE_FIELD, "dependency");
        }
        String scm_id = getOptionalString(materialMap, YAML_SHORT_KEYWORD_SCM_ID);
        if (scm_id != null) {
            material.addProperty(JSON_MATERIAL_TYPE_FIELD, "plugin");
            material.addProperty("scm_id", scm_id);
        }
        String package_id = getOptionalString(materialMap, YAML_SHORT_KEYWORD_PACKAGE_ID);
        if (package_id != null) {
            material.addProperty(JSON_MATERIAL_TYPE_FIELD, "package");
            material.addProperty("package_id", package_id);
        }
        String p4 = getOptionalString(materialMap, YAML_SHORT_KEYWORD_PERFORCE);
        if (p4 != null) {
            material.addProperty(JSON_MATERIAL_TYPE_FIELD, "p4");
            material.addProperty("port", p4);
        }
        //TODO other types

        // copy all other members
        for (Map.Entry<String, Object> materialProp : materialMap.entrySet()) {
            if (yamlSpecialKeywords.contains(materialProp.getKey()))
                continue;
            if (materialProp.getValue() instanceof String)
                material.addProperty(materialProp.getKey(), (String) materialProp.getValue());
        }
        return material;
    }

    private void addInverseFilter(LinkedTreeMap<String, Object> material, LinkedTreeMap<String, Object> filterList) {
        List<String> filter = (List<String>) filterList.get("ignore");
        if (filter != null) {
            material.put("blacklist", filter);
        }
        filter = (List<String>) material.get("whitelist");
        if (filter != null) {
            material.put("whitelist", filter);
        }
    }

    private void addFilter(JsonObject material, Object filterList, String jsonKeyword) {
        JsonObject filter = material.getAsJsonObject("filter");
        if (filter == null) {
            filter = new JsonObject();
            material.add("filter", filter);
        }
        List<String> list = (List<String>) filterList;
        JsonArray jsonIgnores = new JsonArray();
        for (String path : list) {
            jsonIgnores.add(path);
        }
        filter.add(jsonKeyword, jsonIgnores);
    }
}
