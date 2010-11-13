using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// This class implements the various vm::op options that are
    /// available.
    /// </summary>
    public static partial class Ops
    {
        /// <summary>
        /// Creates a type object associated with the given HOW and of the
        /// given representation.
        /// </summary>
        /// <param name="HOW"></param>
        /// <param name="REPRName"></param>
        /// <returns></returns>
        public static RakudoObject type_object_for(ThreadContext TC, RakudoObject HOW, RakudoObject REPRName)
        {
            var REPRNameStr = Ops.unbox_str(TC, REPRName);
            return REPRRegistry.get_REPR_by_name(REPRNameStr).type_object_for(TC, HOW);
        }

        /// <summary>
        /// Create an instance of an object.
        /// </summary>
        /// <param name="WHAT"></param>
        /// <returns></returns>
        public static RakudoObject instance_of(ThreadContext TC, RakudoObject WHAT)
        {
            return WHAT.STable.REPR.instance_of(TC, WHAT);
        }

        /// <summary>
        /// Checks if the representation considers the object defined.
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static RakudoObject repr_defined(ThreadContext TC, RakudoObject Obj)
        {
            return Ops.box_int(TC, Obj.STable.REPR.defined(TC, Obj) ? 1 : 0, TC.DefaultBoolBoxType);
        }

        /// <summary>
        /// Gets the value of an attribute.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject get_attr(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name)
        {
            return Object.STable.REPR.get_attribute(TC, Object, Class, Name);
        }

        /// <summary>
        /// Gets the value of an attribute, using the given hint.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <returns></returns>
        public static RakudoObject get_attr_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name, int Hint)
        {
            return Object.STable.REPR.get_attribute_with_hint(TC, Object, Class, Name, Hint);
        }

        /// <summary>
        /// Binds the value of an attribute to the given value.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        public static RakudoObject bind_attr(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name, RakudoObject Value)
        {
            Object.STable.REPR.bind_attribute(TC, Object, Class, Name, Value);
            return Value;
        }

        /// <summary>
        /// Binds the value of an attribute to the given value, using the
        /// given hint.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Class"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        public static RakudoObject bind_attr_with_hint(ThreadContext TC, RakudoObject Object, RakudoObject Class, string Name, int Hint, RakudoObject Value)
        {
            Object.STable.REPR.bind_attribute_with_hint(TC, Object, Class, Name, Hint, Value);
            return Value;
        }

        /// <summary>
        /// Finds a method to call by name.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Name"></param>
        /// <returns></returns>
        public static RakudoObject find_method(ThreadContext TC, RakudoObject Object, string Name)
        {
            return Object.STable.FindMethod(TC, Object, Name, Hints.NO_HINT);
        }

        /// <summary>
        /// Finds a method to call, using the hint if available.
        /// </summary>
        /// <param name="Object"></param>
        /// <param name="Name"></param>
        /// <param name="Hint"></param>
        /// <returns></returns>
        public static RakudoObject find_method_with_hint(ThreadContext TC, RakudoObject Object, string Name, int Hint)
        {
            return Object.STable.FindMethod(TC, Object, Name, Hint);
        }

        /// <summary>
        /// Invokes the given method.
        /// </summary>
        /// <param name="Invokee"></param>
        /// <param name="Capture"></param>
        /// <returns></returns>
        public static RakudoObject invoke(ThreadContext TC, RakudoObject Invokee, RakudoObject Capture)
        {
            return Invokee.STable.Invoke(TC, Invokee, Capture);
        }

        /// <summary>
        /// Gets the HOW (higher order workings, e.g. meta-package).
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static RakudoObject get_how(ThreadContext TC, RakudoObject Obj)
        {
            return Obj.STable.HOW;
        }

        /// <summary>
        /// Gets the WHAT (type object).
        /// </summary>
        /// <param name="Obj"></param>
        /// <returns></returns>
        public static RakudoObject get_what(ThreadContext TC, RakudoObject Obj)
        {
            return Obj.STable.WHAT;
        }

        /// <summary>
        /// Checks if the given object's type accepts the checked type, using the
        /// type check cache if one was published.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Obj"></param>
        /// <param name="Checkee"></param>
        /// <returns></returns>
        public static RakudoObject type_check(ThreadContext TC, RakudoObject ToCheck, RakudoObject WantedType)
        {
            return ToCheck.STable.TypeCheck(TC, ToCheck, WantedType);
        }

        /// <summary>
        /// Publishes a type check cache. The list of accepted types must use the
        /// P6list representation.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="WHAT"></param>
        /// <param name="TypeList"></param>
        /// <returns></returns>
        public static RakudoObject publish_type_check_cache(ThreadContext TC, RakudoObject WHAT, RakudoObject TypeList)
        {
            var Types = TypeList as P6list.Instance;
            if (Types != null)
            {
                WHAT.STable.TypeCheckCache = Types.Storage.ToArray();
                return TypeList;
            }
            else
            {
                throw new Exception("publish_type_check_cache expects a P6list");
            }
        }
    }
}
