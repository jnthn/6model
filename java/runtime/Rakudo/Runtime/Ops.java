package Rakudo.Runtime;

import java.util.ArrayList;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.Representations.P6list;
import Rakudo.Metamodel.REPRRegistry;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.MultiDispatch.LexicalCandidateFinder;
import Rakudo.Runtime.MultiDispatch.MultiDispatcher;

/// <summary>
/// This class implements the various vm::op options that are
/// available.
/// </summary>
public class Ops
{
    /// <summary>
    /// Creates a type object associated with the given HOW and of the
    /// given representation.
    /// </summary>
    /// <param name="HOW"></param>
    /// <param name="REPRName"></param>
    /// <returns></returns>
    public static RakudoObject type_object_for(ThreadContext tc, RakudoObject HOW, String REPRName)
    {
        return REPRRegistry.get_REPR_by_name(REPRName).type_object_for(tc, HOW);
    }

    /// <summary>
    /// Create an instance of an object.
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public static RakudoObject instance_of(ThreadContext tc, RakudoObject WHAT)
    {
        return WHAT.getSTable().REPR.instance_of(tc, WHAT);
    }

    /// <summary>
    /// Checks if the representation considers the object defined.
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    public static boolean repr_defined(ThreadContext tc, RakudoObject obj)
    {
        return obj.getSTable().REPR.defined(tc, obj);
    }

    /// <summary>
    /// Gets the value of an attribute.
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_attr(ThreadContext tc, RakudoObject object, RakudoObject Class, String name)
    {
        return object.getSTable().REPR.get_attribute(tc, object, Class, name);
    }

    /// <summary>
    /// Gets the value of an attribute, using the given hint.
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    public static RakudoObject get_attr_with_hint(ThreadContext tc, RakudoObject object, RakudoObject Class, String name, int Hint)
    {
        return object.getSTable().REPR.get_attribute_with_hint(tc, object, Class, name, Hint);
    }

    /// <summary>
    /// Binds the value of an attribute to the given value.
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    /// <param name="Hint"></param>
    public static RakudoObject bind_attr_with_hint(ThreadContext tc, RakudoObject object, RakudoObject Class, String name, RakudoObject value)
    {
        object.getSTable().REPR.bind_attribute(tc, object, Class, name, value);
        return value;
    }

    /// <summary>
    /// Binds the value of an attribute to the given value, using the
    /// given hint.
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    /// <param name="Hint"></param>
    public static RakudoObject bind_attr_with_hint(ThreadContext tc, RakudoObject object, RakudoObject Class, String name, int Hint, RakudoObject value)
    {
        object.getSTable().REPR.bind_attribute_with_hint(tc, object, Class, name, Hint, value);
        return value;
    }

    /// <summary>
    /// Finds a method to call by name.
    /// </summary>
    /// <param name="object"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject find_method(ThreadContext tc, RakudoObject object, String name)
    {
        return object.getSTable().FindMethod.FindMethod(tc, object, name, Hints.NO_HINT);
    }

    /// <summary>
    /// Finds a method to call, using the hint if available.
    /// </summary>
    /// <param name="object"></param>
    /// <param name="name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    public static RakudoObject find_method_with_hint(ThreadContext tc, RakudoObject object, String name, int hint)
    {
        return object.getSTable().FindMethod.FindMethod(tc, object, name, hint);
    }

    /// <summary>
    /// Invokes the given method.
    /// </summary>
    /// <param name="Invokee"></param>
    /// <param name="Capture"></param>
    /// <returns></returns>
    public static RakudoObject invoke(ThreadContext tc, RakudoObject invokee, RakudoObject capture)
    {
        return invokee.getSTable().Invoke.Invoke(tc, invokee, capture);
    }

    /// <summary>
    /// Gets the HOW (higher order workings, e.g. meta-package).
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    public static RakudoObject get_how(ThreadContext tc, RakudoObject object)
    {
        return object.getSTable().HOW;
    }

    /// <summary>
    /// Gets the WHAT (type object).
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    public static RakudoObject get_what(ThreadContext tc, RakudoObject object)
    {
        return object.getSTable().WHAT;
    }

    /// <summary>
    /// Boxes a native int into its matching value type.
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_int(ThreadContext tc, int value, RakudoObject to)
    {
        Representation REPR = to.getSTable().REPR;
        RakudoObject result = REPR.instance_of(tc, to);
        REPR.set_int(tc, result, value);
        return result;
    }

    /// <summary>
    /// Boxes a native num into its matching value type.
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_num(ThreadContext tc, double value, RakudoObject to)
    {
        Representation REPR = to.getSTable().REPR;
        RakudoObject result = REPR.instance_of(tc, to);
        REPR.set_num(tc, result, value);
        return result;
    }

    /// <summary>
    /// Boxes a native string into its matching value type.
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_str(ThreadContext tc, String value, RakudoObject to)
    {
        Representation REPR = to.getSTable().REPR;
        RakudoObject result = REPR.instance_of(tc, to);
        REPR.set_str(tc, result, value);
        return result;
    }

    /// <summary>
    /// Unboxes a boxed int.
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static int unbox_int(ThreadContext tc, RakudoObject boxed)
    {
        return boxed.getSTable().REPR.get_int(tc, boxed);
    }

    /// <summary>
    /// Unboxes a boxed num.
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static double unbox_num(ThreadContext tc, RakudoObject boxed)
    {
        return boxed.getSTable().REPR.get_num(tc, boxed);
    }

    /// <summary>
    /// Unboxes a boxed string.
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static String unbox_str(ThreadContext tc, RakudoObject boxed)
    {
        return boxed.getSTable().REPR.get_str(tc, boxed);
    }

    /// <summary>
    /// Coerces an integer into a string.
    /// </summary>
    /// <param name="Int"></param>
    /// <param name="TargetType"></param>
    /// <returns></returns>
    public static RakudoObject coerce_int_to_str(ThreadContext tc, RakudoObject intObject, RakudoObject targetType)
    {
        int value = Ops.unbox_int(tc, intObject);
        return Ops.box_str(tc, Integer.toString(value), targetType);
    }

    /// <summary>
    /// Coerces a floating point number into a string.
    /// </summary>
    /// <param name="Num"></param>
    /// <param name="TargetType"></param>
    /// <returns></returns>
    public static RakudoObject coerce_num_to_str(ThreadContext tc, RakudoObject numObject, RakudoObject targetType)
    {
        double value = Ops.unbox_num(tc, numObject);
        return Ops.box_str(tc, Double.toString(value), targetType);
    }

    /// <summary>
    /// Coerces an integer into a floating point number.
    /// </summary>
    /// <param name="Int"></param>
    /// <param name="TargetType"></param>
    /// <returns></returns>
    public static RakudoObject coerce_int_to_num(ThreadContext tc, RakudoObject intObject, RakudoObject targetType)
    {
        int value = Ops.unbox_int(tc, intObject);
        return Ops.box_num(tc, (double)value, targetType);
    }

    /// <summary>
    /// Coerces a floating point number into an integer.
    /// </summary>
    /// <param name="Int"></param>
    /// <param name="TargetType"></param>
    /// <returns></returns>
    public static RakudoObject coerce_num_to_int(ThreadContext tc, RakudoObject numObject, RakudoObject targetType)
    {
        double value = Ops.unbox_num(tc, numObject);
        return Ops.box_int(tc, (int)value, targetType);
    }

    /// <summary>
    /// Gets a lexical variable of the given name.
    /// </summary>
    /// <param name="i"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_lex(ThreadContext tc, String name)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(name, out Index)) // the C# version
            if (curContext.LexPad.SlotMapping.containsKey(name)) {
                int index = curContext.LexPad.SlotMapping.get(name);
                return curContext.LexPad.Storage[index];
            }
            curContext = curContext.Outer;
        }
        throw new UnsupportedOperationException("No variable " + name + " found in the lexical scope");
    }

    /// <summary>
    /// Binds the given value to a lexical variable of the given name.
    /// </summary>
    /// <param name="i"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject bind_lex(ThreadContext tc, String name, RakudoObject value)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(name, out Index)) // the C# version
            if (curContext.LexPad.SlotMapping.containsKey(name))
            {
                int index = curContext.LexPad.SlotMapping.get(name);
                curContext.LexPad.Storage[index] = value;
                return value;
            }
            curContext = curContext.Outer;
        }
        throw new UnsupportedOperationException("No variable " + name + " found in the lexical scope");
    }

    /// <summary>
    /// Looks up a variable in the dynamic scope.
    /// </summary>
    /// <param name="C"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_dynamic(ThreadContext tc, String name)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(name, out Index)) // the C# version
            if (curContext.LexPad.SlotMapping.containsKey(name)) {
                int index = curContext.LexPad.SlotMapping.get(name);
                return curContext.LexPad.Storage[index];
            }
            curContext = curContext.Caller;
        }
        throw new UnsupportedOperationException("No variable " + name + " found in the dynamic scope");
    }

    /// <summary>
    /// Binds the given value to a variable in the dynamic scope.
    /// </summary>
    /// <param name="C"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject bind_dynamic(ThreadContext tc, String name, RakudoObject value)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
            // if (CurContext.LexPad.SlotMapping.TryGetValue(name, out Index)) // the C# version
            if (curContext.LexPad.SlotMapping.containsKey(name))
            {
                int index = curContext.LexPad.SlotMapping.get(name);
                curContext.LexPad.Storage[index] = value;
                return value;
            }
            curContext = curContext.Caller;
        }
        throw new UnsupportedOperationException("No variable " + name + " found in the dynamic scope");
    }

    /// <summary>
    /// Compares two floating point numbers for equality.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject equal_nums(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_num(tc, x) == Ops.unbox_num(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two integers for equality.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject equal_ints(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_int(tc, x) == Ops.unbox_int(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two strings for equality.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject equal_strs(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_str(tc, x) == Ops.unbox_str(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Logical not.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject logical_not_int(ThreadContext tc, RakudoObject x)
    {
        return Ops.box_int(tc, Ops.unbox_int(tc, x) == 0 ? 1 : 0, tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Performs an integer addition.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject add_int(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc, Ops.unbox_int(tc, x) + Ops.unbox_int(tc, y), tc.DefaultIntBoxType);
    }

    /// <summary>
    /// Performs an integer subtraction.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject sub_int(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc, Ops.unbox_int(tc, x) - Ops.unbox_int(tc, y), tc.DefaultIntBoxType);
    }

    /// <summary>
    /// Performs an integer multiplication.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject mul_int(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc, Ops.unbox_int(tc, x) * Ops.unbox_int(tc, y), tc.DefaultIntBoxType);
    }

    /// <summary>
    /// Performs an integer division.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject div_int(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc, Ops.unbox_int(tc, x) / Ops.unbox_int(tc, y), tc.DefaultIntBoxType);
    }

    /// <summary>
    /// Performs an integer modulo.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject mod_int(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc, Ops.unbox_int(tc, x) % Ops.unbox_int(tc, y), tc.DefaultIntBoxType);
    }

    /// <summary>
    /// Performs a string concatenation.
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject concat(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_str(tc, Ops.unbox_str(tc, x) + Ops.unbox_str(tc, y), tc.DefaultStrBoxType);
    }

    /// <summary>
    /// Entry point to multi-dispatch over the candidates in the inner
    /// dispatcher.
    /// </summary>
    /// <param name="tc"></param>
    /// <returns></returns>
    public static RakudoObject multi_dispatch_over_lexical_candidates(ThreadContext tc, RakudoObject name)
    {
        RakudoObject candidate = MultiDispatcher.FindBestCandidate(
            LexicalCandidateFinder.FindCandidates(
                tc.CurrentContext.Caller,
                tc.CurrentContext.Outer,
                "!" + Ops.unbox_str(tc, name) + "-candidates"),
            tc.CurrentContext.Capture);
        return candidate.getSTable().Invoke.Invoke(tc, candidate, tc.CurrentContext.Capture);
    }

    /// <summary>
    /// Gets a value at a given positional index from a low level list
    /// (something that uses the P6list representation).
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <param name="Index"></param>
    /// <returns></returns>
    public static RakudoObject lllist_get_at_pos(ThreadContext tc, RakudoObject lowlevelList, RakudoObject index)
    {
        if (lowlevelList instanceof P6list.Instance)
        {
            return ((P6list.Instance)lowlevelList).Storage.get(Ops.unbox_int(tc, index));
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use lllist_get_at_pos if representation is not P6list");
        }
    }

    /// <summary>
    /// Binds a value at a given positional index from a low level list
    /// (something that uses the P6list representation).
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <param name="Index"></param>
    /// <returns></returns>
    public static void lllist_bind_at_pos(ThreadContext tc, RakudoObject lowlevelList, RakudoObject indexObj, RakudoObject value)
    {
        if (lowlevelList instanceof P6list.Instance)
        {
            ArrayList<RakudoObject> storage = ((P6list.Instance)lowlevelList).Storage;
            Integer index = Ops.unbox_int(tc, indexObj);
            if (index < storage.size())
            {
                storage.set(index,value);
            }
            else
            {
                // XXX Need some more efficient resizable array approach...
                // Also this is no way thread safe.
                while (index > storage.size())
                    storage.add(null);
                storage.add(value);
            }
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use lllist_bind_at_pos if representation is not P6list");
        }
    }

    /// <summary>
    /// Binds a value at a given positional index from a low level list
    /// (something that uses the P6list representation).
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <param name="Index"></param>
    /// <returns></returns>
    public static RakudoObject lllist_elems(ThreadContext tc, RakudoObject lowlevelList)
    {
        if (lowlevelList instanceof P6list.Instance)
        {
            return Ops.box_int(tc, ((P6list.Instance)lowlevelList).Storage.size(), tc.DefaultIntBoxType);
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use lllist_elems if representation is not P6list");
        }
    }
}
