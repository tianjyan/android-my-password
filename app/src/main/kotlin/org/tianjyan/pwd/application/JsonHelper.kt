package org.tianjyan.pwd.application

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import java.lang.reflect.Array
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * @author keane
 */
object JsonHelper {
    private val TAG = "JSONHelper"

    /**
     * 将对象转换成Json字符串
     * @param obj
     * @return json类型字符串
     */
    fun toJSON(obj: Any): String {
        val js = JSONStringer()
        serialize(js, obj)
        return js.toString()
    }

    /**
     * 序列化为JSON
     * @param js json对象
     * @param o    待需序列化的对象
     */
    private fun serialize(js: JSONStringer, o: Any?) {
        if (isNull(o)) {
            try {
                js.value(null)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return
        }

        val clazz = o!!.javaClass
        if (isObject(clazz)) { // 对象
            serializeObject(js, o)
        } else if (isArray(clazz)) { // 数组
            serializeArray(js, o)
        } else if (isCollection(clazz)) { // 集合
            val collection = o as Collection<*>
            serializeCollect(js, collection)
        } else if (isMap(clazz)) { // 集合
            val collection = o as HashMap<*, *>
            serializeMap(js, collection)
        } else { // 单个值
            try {
                js.value(o)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 序列化数组
     * @param js    json对象
     * @param array    数组
     */
    private fun serializeArray(js: JSONStringer, array: Any) {
        try {
            js.array()
            for (i in 0 until Array.getLength(array)) {
                val o = Array.get(array, i)
                serialize(js, o)
            }
            js.endArray()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 序列化集合
     * @param js    json对象
     * @param collection    集合
     */
    private fun serializeCollect(js: JSONStringer, collection: Collection<*>) {
        try {
            js.array()
            for (o in collection) {
                serialize(js, o)
            }
            js.endArray()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 序列化Map
     * @param js    json对象
     * @param map    map对象
     */
    private fun serializeMap(js: JSONStringer, map: Map<*, *>) {
        try {
            js.`object`()
            val valueMap = map as Map<String, Any>
            for ((key, value) in valueMap) {
                js.key(key)
                serialize(js, value)
            }
            js.endObject()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 序列化对象
     * @param js    json对象
     * @param obj    待序列化对象
     */
    private fun serializeObject(js: JSONStringer, obj: Any) {
        try {
            js.`object`()
            val objClazz = obj.javaClass
            val methods = objClazz.declaredMethods
            val fields = objClazz.declaredFields
            for (field in fields) {
                try {
                    val fieldType = field.type.simpleName
                    val fieldGetName = parseMethodName(field.name, "get")
                    if (!haveMethod(methods, fieldGetName)) {
                        continue
                    }
                    val fieldGetMet = objClazz.getMethod(fieldGetName)
                    val fieldVal = fieldGetMet.invoke(obj)
                    var result: String? = null
                    if ("Date" == fieldType) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        result = sdf.format(fieldVal as Date)

                    } else {
                        if (null != fieldVal) {
                            result = fieldVal.toString()
                        }
                    }
                    js.key(field.name)
                    serialize(js, result)
                } catch (e: Exception) {
                    continue
                }

            }
            js.endObject()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 判断是否存在某属性的 get方法
     * @param methods    引用方法的数组
     * @param fieldMethod    方法名称
     * @return true或者false
     */
    fun haveMethod(methods: kotlin.Array<Method>, fieldMethod: String?): Boolean {
        for (met in methods) {
            if (fieldMethod == met.getName()) {
                return true
            }
        }
        return false
    }

    /**
     * 拼接某属性的 get或者set方法
     * @param fieldName    字段名称
     * @param methodType    方法类型
     * @return 方法名称
     */
    fun parseMethodName(fieldName: String?, methodType: String): String? {
        return if (null == fieldName || "" == fieldName) {
            null
        } else methodType + fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1)
    }

    /**
     * 给字段赋值
     * @param obj  实例对象
     * @param valMap  值集合
     */
    fun setFieldValue(obj: Any, valMap: Map<String, String>) {
        val cls = obj.javaClass
        // 取出bean里的所有方法
        val methods = cls.declaredMethods
        val fields = cls.declaredFields

        for (field in fields) {
            try {
                val setMetodName = parseMethodName(field.name, "set")
                if (!haveMethod(methods, setMetodName)) {
                    continue
                }
                val fieldMethod = cls.getMethod(setMetodName, field
                        .type)
                val value = valMap[field.name]
                if (null != value && "" != value) {
                    val fieldType = field.type.simpleName
                    if ("String" == fieldType) {
                        fieldMethod.invoke(obj, value)
                    } else if ("Date" == fieldType) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        val temp = sdf.parse(value)
                        fieldMethod.invoke(obj, temp)
                    } else if ("Integer" == fieldType || "int" == fieldType) {
                        val intval = Integer.parseInt(value)
                        fieldMethod.invoke(obj, intval)
                    } else if ("Long".equals(fieldType, ignoreCase = true)) {
                        val temp = java.lang.Long.parseLong(value)
                        fieldMethod.invoke(obj, temp)
                    } else if ("Double".equals(fieldType, ignoreCase = true)) {
                        val temp = java.lang.Double.parseDouble(value)
                        fieldMethod.invoke(obj, temp)
                    } else if ("Boolean".equals(fieldType, ignoreCase = true)) {
                        val temp = java.lang.Boolean.parseBoolean(value)
                        fieldMethod.invoke(obj, temp)
                    } else {
                        println("setFieldValue not supper type:" + fieldType)
                    }
                }
            } catch (e: Exception) {
                continue
            }

        }

    }

    /**
     * bean对象转Map
     * @param obj    实例对象
     * @return    map集合
     */
    fun beanToMap(obj: Any): Map<String, String> {
        val cls = obj.javaClass
        val valueMap = HashMap<String, String>()
        // 取出bean里的所有方法
        val methods = cls.declaredMethods
        val fields = cls.declaredFields
        for (field in fields) {
            try {
                val fieldType = field.type.simpleName
                val fieldGetName = parseMethodName(field.name, "get")
                if (!haveMethod(methods, fieldGetName)) {
                    continue
                }
                val fieldGetMet = cls.getMethod(fieldGetName)
                val fieldVal = fieldGetMet.invoke(obj)
                var result: String? = null
                if ("Date" == fieldType) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                    result = sdf.format(fieldVal as Date)

                } else {
                    if (null != fieldVal) {
                        result = fieldVal.toString()
                    }
                }
                valueMap.put(field.name, result!!)
            } catch (e: Exception) {
                continue
            }

        }
        return valueMap

    }

    /**
     * 给对象的字段赋值
     * @param obj    类实例
     * @param fieldSetMethod    字段方法
     * @param fieldType    字段类型
     * @param value
     */
    fun setFiedlValue(obj: Any, fieldSetMethod: Method, fieldType: String, value: Any?) {

        try {
            if (null != value && "" != value) {
                if ("String" == fieldType) {
                    fieldSetMethod.invoke(obj, value.toString())
                } else if ("Date" == fieldType) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                    val temp = sdf.parse(value.toString())
                    fieldSetMethod.invoke(obj, temp)
                } else if ("Integer" == fieldType || "int" == fieldType) {
                    val intval = Integer.parseInt(value.toString())
                    fieldSetMethod.invoke(obj, intval)
                } else if ("Long".equals(fieldType, ignoreCase = true)) {
                    val temp = java.lang.Long.parseLong(value.toString())
                    fieldSetMethod.invoke(obj, temp)
                } else if ("Double".equals(fieldType, ignoreCase = true)) {
                    val temp = java.lang.Double.parseDouble(value.toString())
                    fieldSetMethod.invoke(obj, temp)
                } else if ("Boolean".equals(fieldType, ignoreCase = true)) {
                    val temp = java.lang.Boolean.parseBoolean(value.toString())
                    fieldSetMethod.invoke(obj, temp)
                } else {
                    fieldSetMethod.invoke(obj, value)
                    Log.e(TAG, TAG + ">>>>setFiedlValue -> not supper type" + fieldType)
                }
            }

        } catch (e: Exception) {
            //            Log.e(TAG, TAG  + ">>>>>>>>>>set value error.",e);
            e.printStackTrace()
        }

    }

    /**
     * 反序列化简单对象
     * @param jo    json对象
     * @param clazz    实体类类型
     * @return    反序列化后的实例
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun <T> parseObject(jo: JSONObject, clazz: Class<T>?): T? {
        if (clazz == null || isNull(jo)) {
            return null
        }

        val obj = newInstance(clazz) ?: return null
        if (isMap(clazz)) {
            setField(obj, jo)
        } else {
            // 取出bean里的所有方法
            val methods = clazz.declaredMethods
            val fields = clazz.declaredFields
            for (f in fields) {
                val setMetodName = parseMethodName(f.name, "set")
                if (!haveMethod(methods, setMetodName)) {
                    continue
                }
                try {
                    val fieldMethod = clazz.getMethod(setMetodName, f.type)
                    setField(obj, fieldMethod, f, jo)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return obj
    }

    /**
     * 反序列化简单对象
     * @param jsonStr    json字符串
     * @param clazz    实体类类型
     * @return    反序列化后的实例
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun <T> parseObject(jsonStr: String?, clazz: Class<T>?): T? {
        if (clazz == null || jsonStr == null || jsonStr.length == 0) {
            return null
        }

        val jo: JSONObject
        jo = JSONObject(jsonStr)
        return if (isNull(jo)) {
            null
        } else parseObject(jo, clazz)

    }

    /**
     * 反序列化数组对象
     * @param ja    json数组
     * @param clazz    实体类类型
     * @return    反序列化后的数组
     */
    fun <T> parseArray(ja: JSONArray, clazz: Class<T>?): kotlin.Array<T>? {
        if (clazz == null || isNull(ja)) {
            return null
        }

        val len = ja.length()

        val array = Array.newInstance(clazz, len) as kotlin.Array<T>

        for (i in 0 until len) {
            try {
                val jo = ja.getJSONObject(i)
                val o = parseObject(jo, clazz)
                array[i] = o!!
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        return array
    }


    /**
     * 反序列化数组对象
     * @param jsonStr    json字符串
     * @param clazz    实体类类型
     * @return    序列化后的数组
     */
    fun <T> parseArray(jsonStr: String?, clazz: Class<T>?): kotlin.Array<T>? {
        if (clazz == null || jsonStr == null || jsonStr.length == 0) {
            return null
        }
        var jo: JSONArray? = null
        try {
            jo = JSONArray(jsonStr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return if (isNull(jo)) {
            null
        } else parseArray(jo!!, clazz)

    }

    /**
     * 反序列化泛型集合
     * @param ja    json数组
     * @param collectionClazz    集合类型
     * @param genericType    实体类类型
     * @return
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun <T> parseCollection(ja: JSONArray, collectionClazz: Class<*>?,
                            genericType: Class<T>?): Collection<T>? {

        if (collectionClazz == null || genericType == null || isNull(ja)) {
            return null
        }

        val collection = newInstance(collectionClazz) as Collection<T>?

        for (i in 0 until ja.length()) {
            try {
                val jo = ja.getJSONObject(i)
                val o = parseObject(jo, genericType)
                collection!!.plus(o)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        return collection
    }

    /**
     * 反序列化泛型集合
     * @param jsonStr    json字符串
     * @param collectionClazz    集合类型
     * @param genericType    实体类类型
     * @return    反序列化后的数组
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun <T> parseCollection(jsonStr: String?, collectionClazz: Class<*>?,
                            genericType: Class<T>?): Collection<T>? {
        if (collectionClazz == null || genericType == null || jsonStr == null
                || jsonStr.length == 0) {
            return null
        }
        var jo: JSONArray? = null
        try {
            //如果为数组，则此处转化时，需要去掉前面的键，直接后面的[]中的值
            val index = jsonStr.indexOf("[")
            var arrayString: String? = null

            //获取数组的字符串
            if (-1 != index) {
                arrayString = jsonStr.substring(index)
            }

            //如果为数组，使用数组转化
            if (null != arrayString) {
                jo = JSONArray(arrayString)
            } else {
                jo = JSONArray(jsonStr)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return if (isNull(jo)) {
            null
        } else parseCollection(jo!!, collectionClazz, genericType)

    }

    /**
     * 根据类型创建对象
     * @param clazz    待创建实例的类型
     * @return    实例对象
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun <T> newInstance(clazz: Class<T>?): T? {
        if (clazz == null)
            return null
        val obj: T
        if (clazz.isInterface) {
            if (clazz == Map::class.java) {
                obj = HashMap<Any, Any>() as T
            } else if (clazz == List::class.java) {
                obj = ArrayList<Any>() as T
            } else if (clazz == Set::class.java) {
                obj = HashSet<Any>() as T
            } else {
                throw JSONException("unknown interface: " + clazz)
            }
        } else {
            try {
                obj = clazz.newInstance()
            } catch (e: Exception) {
                throw JSONException("unknown class type: " + clazz)
            }

        }
        return obj
    }

    /**
     * 设定Map的值
     * @param obj    待赋值字段的对象
     * @param jo    json实例
     */
    private fun setField(obj: Any, jo: JSONObject) {
        try {
            val keyIter = jo.keys()
            var key: String
            var value: Any
            val valueMap = obj as Map<String, Any>
            while (keyIter.hasNext()) {
                key = keyIter.next()
                value = jo.get(key)
                valueMap.plus(Pair(key, value))

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * 设定字段的值
     * @param obj    待赋值字段的对象
     * @param fieldSetMethod    字段方法名
     * @param field    字段
     * @param jo    json实例
     */
    private fun setField(obj: Any, fieldSetMethod: Method, field: Field, jo: JSONObject) {
        val name = field.name
        val clazz = field.type
        try {
            if (isArray(clazz)) { // 数组
                val c = clazz.componentType
                val ja = jo.optJSONArray(name)
                if (!isNull(ja)) {
                    val array = parseArray(ja, c)
                    setFiedlValue(obj, fieldSetMethod, clazz.simpleName, array)
                }
            } else if (isCollection(clazz)) { // 泛型集合
                // 获取定义的泛型类型
                var c: Class<*>? = null
                val gType = field.genericType
                if (gType is ParameterizedType) {
                    val targs = gType.actualTypeArguments
                    if (targs != null && targs.size > 0) {
                        val t = targs[0]
                        c = t as Class<*>
                    }
                }

                val ja = jo.optJSONArray(name)
                if (!isNull(ja)) {
                    val o = parseCollection(ja, clazz, c)
                    setFiedlValue(obj, fieldSetMethod, clazz.simpleName, o)
                }
            } else if (isSingle(clazz)) { // 值类型
                val o = jo.opt(name)
                if (o != null && o.toString() != "null") {
                    setFiedlValue(obj, fieldSetMethod, clazz.simpleName, o)
                }
            } else if (isObject(clazz)) { // 对象
                val j = jo.optJSONObject(name)
                if (!isNull(j)) {
                    val o = parseObject(j, clazz)
                    setFiedlValue(obj, fieldSetMethod, clazz.simpleName, o)
                }
            } else if (isList(clazz)) { // 列表
                //				JSONObject j = jo.optJSONObject(name);
                //				if (!isNull(j)) {
                //					Object o = parseObject(j, clazz);
                //					f.set(obj, o);
                //				}
            } else {
                throw Exception("unknow type!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 设定字段的值
     * @param obj    待赋值字段的对象
     * @param field    字段
     * @param jo    json实例
     */
    private fun setField(obj: Any, field: Field, jo: JSONObject) {
        val name = field.name
        val clazz = field.type
        try {
            if (isArray(clazz)) { // 数组
                val c = clazz.componentType
                val ja = jo.optJSONArray(name)
                if (!isNull(ja)) {
                    val array = parseArray(ja, c)
                    field.set(obj, array)
                }
            } else if (isCollection(clazz)) { // 泛型集合
                // 获取定义的泛型类型
                var c: Class<*>? = null
                val gType = field.genericType
                if (gType is ParameterizedType) {
                    val targs = gType.actualTypeArguments
                    if (targs != null && targs.size > 0) {
                        val t = targs[0]
                        c = t as Class<*>
                    }
                }
                val ja = jo.optJSONArray(name)
                if (!isNull(ja)) {
                    val o = parseCollection(ja, clazz, c)
                    field.set(obj, o)
                }
            } else if (isSingle(clazz)) { // 值类型
                val o = jo.opt(name)
                if (o != null) {
                    field.set(obj, o)
                }
            } else if (isObject(clazz)) { // 对象
                val j = jo.optJSONObject(name)
                if (!isNull(j)) {
                    val o = parseObject(j, clazz)
                    field.set(obj, o)
                }
            } else if (isList(clazz)) { // 列表
                val j = jo.optJSONObject(name)
                if (!isNull(j)) {
                    val o = parseObject(j, clazz)
                    field.set(obj, o)
                }
            } else {
                throw Exception("unknow type!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 判断对象是否为空
     * @param obj    实例
     * @return
     */
    private fun isNull(obj: Any?): Boolean {
        return if (obj is JSONObject) {
            JSONObject.NULL == obj
        } else obj == null
    }

    /**
     * 判断是否是值类型
     * @param clazz
     * @return
     */
    private fun isSingle(clazz: Class<*>): Boolean {
        return isBoolean(clazz) || isNumber(clazz) || isString(clazz)
    }

    /**
     * 是否布尔值
     * @param clazz
     * @return
     */
    fun isBoolean(clazz: Class<*>?): Boolean {
        return clazz != null && (java.lang.Boolean.TYPE.isAssignableFrom(clazz) || Boolean::class.java
                .isAssignableFrom(clazz))
    }

    /**
     * 是否数值
     * @param clazz
     * @return
     */
    fun isNumber(clazz: Class<*>?): Boolean {
        return clazz != null && (java.lang.Byte.TYPE.isAssignableFrom(clazz) || java.lang.Short.TYPE.isAssignableFrom(clazz)
                || Integer.TYPE.isAssignableFrom(clazz)
                || java.lang.Long.TYPE.isAssignableFrom(clazz)
                || java.lang.Float.TYPE.isAssignableFrom(clazz)
                || java.lang.Double.TYPE.isAssignableFrom(clazz) || Number::class.java
                .isAssignableFrom(clazz))
    }

    /**
     * 判断是否是字符串
     * @param clazz
     * @return
     */
    fun isString(clazz: Class<*>?): Boolean {
        return clazz != null && (String::class.java.isAssignableFrom(clazz)
                || Character.TYPE.isAssignableFrom(clazz) || Char::class.java
                .isAssignableFrom(clazz))
    }

    /**
     * 判断是否是对象
     * @param clazz
     * @return
     */
    private fun isObject(clazz: Class<*>?): Boolean {
        return clazz != null && !isSingle(clazz) && !isArray(clazz) && !isCollection(clazz) && !isMap(clazz)
    }

    /**
     * 判断是否是数组
     * @param clazz
     * @return
     */
    fun isArray(clazz: Class<*>?): Boolean {
        return clazz != null && clazz.isArray
    }

    /**
     * 判断是否是集合
     * @param clazz
     * @return
     */
    fun isCollection(clazz: Class<*>?): Boolean {
        return clazz != null && Collection::class.java.isAssignableFrom(clazz)
    }

    /**
     * 判断是否是Map
     * @param clazz
     * @return
     */
    fun isMap(clazz: Class<*>?): Boolean {
        return clazz != null && Map::class.java.isAssignableFrom(clazz)
    }

    /**
     * 判断是否是列表
     * @param clazz
     * @return
     */
    fun isList(clazz: Class<*>?): Boolean {
        return clazz != null && List::class.java.isAssignableFrom(clazz)
    }

}