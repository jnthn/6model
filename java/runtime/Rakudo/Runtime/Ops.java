// Ops.java is equivalent to the combination of partial classes in the
// dotnet/runtime/Runtime/Ops directory, in the following files:
//   Boxing.cs     ControlFlow.cs   Library.cs   P6list.cs    Variables.cs
//   Coercion.cs   Dispatch.cs      Metamodel.cs P6mapping.cs
//   Comparison.cs Introspection.cs P6capture.cs Primitive.cs
package Rakudo.Runtime;

import java.util.ArrayList;
import java.util.HashMap;
import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.Representations.P6int;
import Rakudo.Metamodel.Representations.P6list;
import Rakudo.Metamodel.Representations.P6mapping;
import Rakudo.Metamodel.Representations.P6num;
import Rakudo.Metamodel.Representations.P6str;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Metamodel.REPRRegistry;
import Rakudo.Metamodel.SharedTable;
import Rakudo.Runtime.Exceptions.ExceptionDispatcher;
import Rakudo.Runtime.Exceptions.Handler;
import Rakudo.Runtime.Exceptions.LeaveStackUnwinderException;
import Rakudo.Runtime.MultiDispatch.MultiDispatcher;

/// <summary>
/// This class implements the various vm::op options that are available.
/// </summary>
public class Ops  // public static in the C# version
{
    // Methods are grouped in here by the corresponing C# filename

    // Boxing.cs
    /// <summary>
    /// Boxes a native int into its matching value type.  See Boxing.cs
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
    /// Boxes a native int into its matching value type.  See Boxing.cs
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_int(ThreadContext tc, int value)
    {
        RakudoObject result = tc.DefaultIntBoxType.getSTable().REPR.instance_of(tc, tc.DefaultIntBoxType);
        tc.DefaultIntBoxType.getSTable().REPR.set_int(tc, result, value);
        return result;
    }

    /// <summary>
    /// Boxes a native num into its matching value type. See Boxing.cs
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
    /// Boxes a native num into its matching value type. See Boxing.cs
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_num(ThreadContext tc, int value)
    {
        RakudoObject result = tc.DefaultNumBoxType.getSTable().REPR.instance_of(tc, tc.DefaultNumBoxType);
        tc.DefaultNumBoxType.getSTable().REPR.set_num(tc, result, value);
        return result;
    }

    /// <summary>
    /// Boxes a native string into its matching value type. See Boxing.cs
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
    /// Boxes a native string into its matching value type. See Boxing.cs
    /// </summary>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject box_str(ThreadContext tc, String value)
    {
        RakudoObject result = tc.DefaultStrBoxType.getSTable().REPR.instance_of(tc, tc.DefaultStrBoxType);
        tc.DefaultStrBoxType.getSTable().REPR.set_str(tc, result, value);
        return result;
    }

    /// <summary>
    /// Unboxes a boxed int. See Boxing.cs
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static int unbox_int(ThreadContext tc, RakudoObject boxed)
    {
        return boxed.getSTable().REPR.get_int(tc, boxed);
    }

    /// <summary>
    /// Unboxes a boxed num. See Boxing.cs
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static double unbox_num(ThreadContext tc, RakudoObject boxed)
    {
        return boxed.getSTable().REPR.get_num(tc, boxed);
    }

    /// <summary>
    /// Unboxes a boxed string. See Boxing.cs
    /// </summary>
    /// <param name="Boxed"></param>
    /// <returns></returns>
    public static String unbox_str(ThreadContext tc, RakudoObject boxed)
    {
        return boxed.getSTable().REPR.get_str(tc, boxed);
    }

    /// <summary>
    /// Creates a type object associated with the given HOW and of the
    /// given representation. See Representation.cs
    /// </summary>
    /// <param name="HOW"></param>
    /// <param name="REPRName"></param>
    /// <returns></returns>
    public static RakudoObject type_object_for(ThreadContext tc, RakudoObject HOW, RakudoObject REPRName)
    {
        String strREPRName = Ops.unbox_str(tc, REPRName);
        return REPRRegistry.get_REPR_by_name(strREPRName).type_object_for(tc, HOW);
    }

    /// <summary>
    /// Create an instance of an object. See Representation.cs
    /// </summary>
    /// <param name="WHAT"></param>
    /// <returns></returns>
    public static RakudoObject instance_of(ThreadContext tc, RakudoObject WHAT)
    {
        return WHAT.getSTable().REPR.instance_of(tc, WHAT);
    }

    /// <summary>
    /// Checks if the representation considers the object defined. See Representation.cs
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    public static RakudoObject repr_defined(ThreadContext tc, RakudoObject obj)
    {
        return Ops.box_int(tc, obj.getSTable().REPR.defined(tc, obj) ? 1 : 0, tc.DefaultBoolBoxType );
    }

    /// <summary>
    /// Gets the value of an attribute. See Representation.cs
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_attr(ThreadContext tc, RakudoObject object, RakudoObject classObj, String name)
    {
        return object.getSTable().REPR.get_attribute(tc, object, classObj, name);
    }

    /// <summary>
    /// Gets the value of an attribute, using the given hint. See Representation.cs
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    /// <param name="hint"></param>
    /// <returns></returns>
    public static RakudoObject get_attr_with_hint(ThreadContext tc, RakudoObject object, RakudoObject classObj, String name, int hint)
    {
        return object.getSTable().REPR.get_attribute_with_hint(tc, object, classObj, name, hint);
    }

    /// <summary>
    /// Binds the value of an attribute to the given value. See Representation.cs
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    public static RakudoObject bind_attr(ThreadContext tc, RakudoObject object, RakudoObject classObj, String name, RakudoObject value)
    {
        object.getSTable().REPR.bind_attribute(tc, object, classObj, name, value);
        return value;
    }

    /// <summary>
    /// Binds the value of an attribute to the given value, using the
    /// given hint. See Representation.cs
    /// </summary>
    /// <param name="object"></param>
    /// <param name="Class"></param>
    /// <param name="name"></param>
    /// <param name="Hint"></param>
    public static RakudoObject bind_attr_with_hint(ThreadContext tc, RakudoObject object, RakudoObject classObj, String name, int hint, RakudoObject value)
    {
        object.getSTable().REPR.bind_attribute_with_hint(tc, object, classObj, name, hint, value);
        return value;
    }

    /// <summary>
    /// Finds a method to call by name. See Representation.cs
    /// </summary>
    /// <param name="object"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject find_method(ThreadContext tc, RakudoObject object, String name)
    {
        return object.getSTable().FindMethod(tc, object, name, Hints.NO_HINT);
    }

    /// <summary>
    /// Finds a method to call, using the hint if available. See Representation.cs
    /// </summary>
    /// <param name="object"></param>
    /// <param name="name"></param>
    /// <param name="Hint"></param>
    /// <returns></returns>
    public static RakudoObject find_method_with_hint(ThreadContext tc, RakudoObject object, String name, int hint)
    {
        return object.getSTable().FindMethod(tc, object, name, hint);
    }

    /// <summary>
    /// Invokes the given method. See Representation.cs
    /// </summary>
    /// <param name="Invokee"></param>
    /// <param name="Capture"></param>
    /// <returns></returns>
    public static RakudoObject invoke(ThreadContext tc, RakudoObject invokee, RakudoObject capture)
    {
        return invokee.getSTable().Invoke(tc, invokee, capture);
    }

    /// <summary>
    /// Gets the HOW (higher order workings, e.g. meta-package). See Representation.cs
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    public static RakudoObject get_how(ThreadContext tc, RakudoObject object)
    {
        return object.getSTable().HOW;
    }

    /// <summary>
    /// Gets the WHAT (type object). See Representation.cs
    /// </summary>
    /// <param name="obj"></param>
    /// <returns></returns>
    public static RakudoObject get_what(ThreadContext tc, RakudoObject object)
    {
        return object.getSTable().WHAT;
    }

    // Coercion.cs
    /// <summary>
    /// Coerces an integer into a string. See Coercion.cs
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
    /// Coerces a floating point number into a string. See Coercion.cs
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
    /// Coerces an integer into a floating point number.  See Coercion.cs
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
    /// Coerces a floating point number into an integer.  See Coercion.cs
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
    /// Coerces a string into an integer.  See Coercion.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Str"></param>
    /// <param name="TargetType"></param>
    /// <returns></returns>
    public static RakudoObject coerce_str_to_int(ThreadContext tc, RakudoObject strObject, RakudoObject targetType)
    {
        int value = Integer.parseInt(Ops.unbox_str(tc, strObject));
        return Ops.box_int(tc, value, targetType);
    }


    /// <summary>
    /// Coerces a string into an number.  See Coercion.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Str"></param>
    /// <param name="TargetType"></param>
    /// <returns></returns>
    public static RakudoObject coerce_str_to_num(ThreadContext tc, RakudoObject strObject, RakudoObject targetType)
    {
        double value = Double.parseDouble(Ops.unbox_str(tc, strObject));
        return Ops.box_num(tc, value, targetType);
    }


    /// <summary>
    /// Compares two floating point numbers for equality.  See Comparison.cs
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
    /// Compares two integers for equality. See Comparison.cs
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
    /// Compares two strings for equality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject equal_strs(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_str(tc, x).equals(Ops.unbox_str(tc, y)) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares reference equality. See Comparison.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <returns></returns>
    public static RakudoObject equal_refs(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc, x == y ? 1 : 0, tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two floating point numbers for less-than inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject less_than_nums(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_num(tc, x) < Ops.unbox_num(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two integers for less-than inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject less_than_ints(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_int(tc, x) < Ops.unbox_int(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two floating point numbers for less-than-or-equal inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject less_than_or_equal_nums(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_num(tc, x) <= Ops.unbox_num(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two integers for less-than-or-equal inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject less_than_or_equal_ints(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_int(tc, x) <= Ops.unbox_int(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two floating point numbers for greater-than inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject greater_than_nums(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_num(tc, x) > Ops.unbox_num(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two integers for greater-than inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject greater_than_ints(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_int(tc, x) > Ops.unbox_int(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two floating point numbers for greater-than-or-equal inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject greater_than_or_equal_nums(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_num(tc, x) >= Ops.unbox_num(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Compares two integers for greater-than-or-equal inequality. See Comparison.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject greater_than_or_equal_ints(ThreadContext tc, RakudoObject x, RakudoObject y)
    {
        return Ops.box_int(tc,
            (Ops.unbox_int(tc, x) >= Ops.unbox_int(tc, y) ? 1 : 0),
            tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Logical not. See Primitive.cs
    /// </summary>
    /// <param name="x"></param>
    /// <param name="ResultType"></param>
    /// <returns></returns>
    public static RakudoObject logical_not_int(ThreadContext tc, RakudoObject x)
    {
        return Ops.box_int(tc, Ops.unbox_int(tc, x) == 0 ? 1 : 0, tc.DefaultBoolBoxType);
    }

    /// <summary>
    /// Performs an integer addition. See Primitive.cs
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
    /// Performs an integer subtraction. See Primitive.cs
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
    /// Performs an integer multiplication. See Primitive.cs
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
    /// Performs an integer division. See Primitive.cs
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
    /// Performs an integer modulo. See Primitive.cs
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
    /// Performs a string concatenation. See Primitive.cs
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
    /// dispatcher. See Dispatch.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <returns></returns>
    public static RakudoObject multi_dispatch_over_lexical_candidates(ThreadContext tc)
    {
        Context curOuter = tc.CurrentContext;
        while (curOuter != null)
        {
            RakudoCodeRef.Instance codeObj = curOuter.StaticCodeObject;
            if (codeObj.Dispatchees != null)
            {
                RakudoObject candidate = MultiDispatcher.FindBestCandidate(
                    codeObj, curOuter.Capture);
                return candidate.getSTable().Invoke(tc, candidate, curOuter.Capture);
            }
            curOuter = curOuter.Outer;
        }
        throw new UnsupportedOperationException("Could not find dispatchee list!");
    }

    /// <summary>
    /// Sets the dispatches of the given code object. Expects something with
    /// RakudoCodeRef and P6list representation respectively. See Dispatch.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="CodeObject"></param>
    /// <param name="Dispatchees"></param>
    /// <returns></returns>
    public static RakudoObject set_dispatchees(ThreadContext tc, RakudoObject codeObject, RakudoObject dispatchees)
    {
        RakudoCodeRef.Instance code = (RakudoCodeRef.Instance)codeObject;
        P6list.Instance dispatchList = (P6list.Instance)dispatchees;
        if (code != null && dispatchList != null)
        {
            code.Dispatchees = (RakudoObject[])dispatchList.Storage.toArray();
            return code;
        }
        else
        {
            throw new UnsupportedOperationException("set_dispatchees must be passed a RakudoCodeRef and a P6list.");
        }
    }

    /// <summary>
    /// Creates an instantiation of the dispatch routine (or proto, which may
    /// serve as one) supplied and augments it with the provided candidates.
    /// It relies on being passed the instantiation of the dispatcher from the
    /// last outer scope that had an instantiation, and we thus take its
    /// candidates. This may or may not hold up in the long run; it works out
    /// in the Perl 6-y "you can make a new instance from any object" sense
    /// though, and seems more likely to get the closure semantics right than
    /// any of the other approaches I've considered so far. See Dispatch.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="ToInstantiate"></param>
    /// <param name="ExtraDispatchees"></param>
    /// <returns></returns>
    public static RakudoObject create_dispatch_and_add_candidates(ThreadContext tc, RakudoObject toInstantiate, RakudoObject extraDispatchees)
    {
        // Make sure we got the right things.
        RakudoCodeRef.Instance source = (RakudoCodeRef.Instance)toInstantiate;
        P6list.Instance additionalDispatchList = (P6list.Instance)extraDispatchees;
        if (source == null || additionalDispatchList == null)
            throw new UnsupportedOperationException("create_dispatch_and_add_candidates expects a RakudoCodeRef and a P6list");

        // Clone all but SC (since it's a new object and doesn't live in any
        // SC yet) and dispatchees (which we want to munge).
        RakudoCodeRef.Instance newDispatch = (RakudoCodeRef.Instance)source.getSTable().REPR.instance_of(tc,source.getSTable().WHAT);
        // XXX the above line works differently in the C# version, more
        // like the line below, but that got a compiler error: an enclosing instance ... is required
        // RakudoCodeRef.Instance newDispatch = new RakudoCodeRef.Instance(source.getSTable());
        newDispatch.Body = source.Body;
        newDispatch.CurrentContext = source.CurrentContext;
        newDispatch.Handlers = source.Handlers;
        newDispatch.OuterBlock = source.OuterBlock;
        newDispatch.OuterForNextInvocation = source.OuterForNextInvocation;
        newDispatch.Sig = source.Sig;
        newDispatch.StaticLexPad = source.StaticLexPad;

        // Take existing candidates and add new ones.
        newDispatch.Dispatchees = new RakudoObject[source.Dispatchees.length + additionalDispatchList.Storage.size()];
        int i = 0;
        for (int j = 0; j < source.Dispatchees.length; j++)
            newDispatch.Dispatchees[i++] = source.Dispatchees[j];
        for (int j = 0; j < additionalDispatchList.Storage.size(); j++)
            newDispatch.Dispatchees[i++] = additionalDispatchList.Storage.get(j);

        return newDispatch;
    }

    /// <summary>
    /// Adds a single new candidate to the end of a dispatcher's candidate
    /// list. See Dispatch.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Dispatcher"></param>
    /// <param name="Dispatchee"></param>
    /// <returns></returns>
    public static RakudoObject push_dispatchee(ThreadContext tc, RakudoObject dispatcher, RakudoObject dispatchee)
    {
        // Validate that we've got something we can push a new dispatchee on to.
        RakudoCodeRef.Instance target = (RakudoCodeRef.Instance)dispatcher;
        if (target == null)
            throw new UnsupportedOperationException("push_dispatchee expects a RakudoCodeRef");
        if (target.Dispatchees == null)
            throw new UnsupportedOperationException("push_dispatchee passed something that is not a dispatcher");

        // Add it.
        RakudoObject[] newList = new RakudoObject[target.Dispatchees.length + 1];
        for (int i = 0; i < target.Dispatchees.length; i++)
            newList[i] = target.Dispatchees[i];
        newList[target.Dispatchees.length] = dispatchee;
        target.Dispatchees = newList;

        return target;
    }

    /// <summary>
    /// Checks if a routine is considered a dispatcher (that is, if it has a
    /// candidate list). See Dispatch.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Check"></param>
    /// <returns></returns>
    public static RakudoObject is_dispatcher(ThreadContext tc, RakudoObject check)
    {
        RakudoCodeRef.Instance checkee = (RakudoCodeRef.Instance)check;
        return Ops.box_int(
            tc,
            (checkee != null && checkee.Dispatchees != null) ? 1 : 0,
            tc.DefaultBoolBoxType
        );
    }

    /// <summary>
    /// Gets a value at a given positional index from a low level list
    /// (something that uses the P6list representation). See P6list.cs
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
    /// (something that uses the P6list representation). See P6list.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <param name="Index"></param>
    /// <returns></returns>
    public static RakudoObject lllist_bind_at_pos(ThreadContext tc, RakudoObject lowlevelList, RakudoObject indexObj, RakudoObject value)
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
            return value;
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use lllist_bind_at_pos if representation is not P6list");
        }
    }

    /// <summary>
    /// Binds a value at a given positional index from a low level list
    /// (something that uses the P6list representation). See P6list.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
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

    /// <summary>
    /// Pushes a value to a low level list (something that
    /// uses the P6list representation). See P6list.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <returns></returns>
    public static RakudoObject lllist_push(ThreadContext tc, RakudoObject LLList, RakudoObject item)
    {
        if (LLList instanceof P6list.Instance)
        {
            ((P6list.Instance)LLList).Storage.add(item);
            return item;
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use lllist_push if representation is not P6list");
        }
    }

    /// <summary>
    /// Pops a value from a low level list (something that
    /// uses the P6list representation). See P6list.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <returns></returns>
    public static RakudoObject lllist_pop(ThreadContext tc, RakudoObject lowlevelList)
    {
        if (lowlevelList instanceof P6list.Instance)
        {
            ArrayList<RakudoObject> store = ((P6list.Instance)lowlevelList).Storage;
            int idx = store.size() - 1;
            if (idx < 0)
            {
                throw new IndexOutOfBoundsException("Cannot pop from an empty list");
            }
            RakudoObject item = store.get(idx);
            store.remove(idx);
            return item;
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use lllist_pop if representation is not P6list");
        }
    }

    /// <summary>
    /// Shifts a value from a low level list (something that
    /// uses the P6list representation). See P6list.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <returns></returns>
    public static RakudoObject lllist_shift(ThreadContext tc, RakudoObject lowlevelList)
    {
        if (lowlevelList instanceof P6list.Instance)
        {
            ArrayList<RakudoObject> store = ((P6list.Instance)lowlevelList).Storage;
            int idx = store.size() - 1;
            if (idx < 0)
            {
                throw new IndexOutOfBoundsException("Cannot shift from an empty list");
            }
            RakudoObject item = store.get(0);
            store.remove(0);
            return item;
        }
        else
        {
            throw new RuntimeException("Cannot use lllist_shift if representation is not P6list");
        }
    }

    /// <summary>
    /// Unshifts a value to a low level list (something that
    /// uses the P6list representation). See P6list.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLList"></param>
    /// <returns></returns>
    public static RakudoObject lllist_unshift(ThreadContext tc, RakudoObject lowlevelList, RakudoObject item)
    {
        if (lowlevelList instanceof P6list.Instance)
        {
            ArrayList<RakudoObject> store = ((P6list.Instance)lowlevelList).Storage;
            store.add(0, item);
            return item;
        }
        else
        {
            throw new RuntimeException("Cannot use lllist_unshift if representation is not P6list");
        }
    }

    /// <summary>
    /// Gets a value at a given key from a low level mapping (something that
    /// uses the P6mapping representation). See P6mapping.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLMapping"></param>
    /// <param name="Key"></param>
    /// <returns></returns>
    public static RakudoObject llmapping_get_at_key(ThreadContext tc, RakudoObject lowlevelMapping, RakudoObject key)
    {
        if (lowlevelMapping instanceof P6mapping.Instance)
        {
            HashMap<String,RakudoObject> storage = ((P6mapping.Instance)lowlevelMapping).Storage;
            String strKey = Ops.unbox_str(tc, key);
            if (storage.containsKey(strKey))
                return storage.get(strKey);
            else
                return null;
        }
        else
        {
            throw new RuntimeException("Cannot use llmapping_get_at_key if representation is not P6mapping");
        }
    }

    /// <summary>
    /// Binds a value at a given key from a low level mapping (something that
    /// uses the P6mapping representation). See P6mapping.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLMapping"></param>
    /// <param name="Key"></param>
    /// <param name="Value"></param>
    /// <returns></returns>
    public static RakudoObject llmapping_bind_at_key(ThreadContext tc, RakudoObject lowlevelMapping, RakudoObject key, RakudoObject value)
    {
        if (lowlevelMapping instanceof P6mapping.Instance)
        {
            HashMap<String,RakudoObject> storage = ((P6mapping.Instance)lowlevelMapping).Storage;
            String strKey = Ops.unbox_str(tc, key);

            // this space intentionally left blank ;)

            storage.put(strKey, value);
            return value;
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use llmapping_bind_at_key if representation is not P6mapping");
        }
    }

    /// <summary>
    /// Gets the number of elements in a low level mapping (something that
    /// uses the P6mapping representation). See P6mapping.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="LLMapping"></param>
    /// <returns></returns>
    public static RakudoObject llmapping_elems(ThreadContext tc, RakudoObject lowlevelMapping)
    {
        if (lowlevelMapping instanceof P6mapping.Instance)
        {
            return Ops.box_int(tc, ((P6mapping.Instance)lowlevelMapping).Storage.size(), tc.DefaultIntBoxType);
        }
        else
        {
            throw new UnsupportedOperationException("Cannot use llmapping_elems if representation is not P6mapping");
        }
    }

    /// <summary>
    /// If the first passed object reference is not null, returns it. Otherwise,
    /// returns the second passed object reference. (Note, we should one day drop
    /// this and implement it as a compiler transformation, to avoid having to
    /// look up the thing to vivify). See ControlFlow.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Check"></param>
    /// <param name="VivifyWith"></param>
    /// <returns></returns>
    public static RakudoObject vivify(ThreadContext tc, RakudoObject check, RakudoObject vivifyWith)
    {
        return (check != null) ? check : vivifyWith;
    }

    /// <summary>
    /// Leaves the specified block, returning the specified value from it. This
    /// unwinds the stack. See ControlFlow.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Block"></param>
    /// <param name="ReturnValue"></param>
    /// <returns></returns>
    public static RakudoObject leave_block(ThreadContext tc, RakudoObject block, RakudoObject returnValue)
    {
        throw new LeaveStackUnwinderException((RakudoCodeRef.Instance)block, returnValue);
    }

    /// <summary>
    /// Throws the specified exception, looking for an exception handler in the
    /// dynamic scope. See ControlFlow.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="ExceptionObject"></param>
    /// <param name="ExceptionType"></param>
    /// <returns></returns>
    public static RakudoObject throw_dynamic(ThreadContext tc, RakudoObject exceptionObject, RakudoObject exceptionType)
    {
        int wantType = Ops.unbox_int(tc, exceptionType);
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
            if (curContext.StaticCodeObject != null)
            {
                Handler[] handlers = curContext.StaticCodeObject.Handlers;
                if (handlers != null)
                    for (int i = 0; i < handlers.length; i++)
                        if (handlers[i].Type == wantType)
                            return ExceptionDispatcher.CallHandler(tc,
                                handlers[i].HandleBlock, exceptionObject);
            }
            curContext = curContext.Caller;
        }
        ExceptionDispatcher.DieFromUnhandledException(tc, exceptionObject);
        return null; // Unreachable; above call exits always.
    }

    /// <summary>
    /// Throws the specified exception, looking for an exception handler in the
    /// lexical scope. See ControlFlow.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="ExceptionObject"></param>
    /// <param name="ExceptionType"></param>
    /// <returns></returns>
    public static RakudoObject throw_lexical(ThreadContext tc, RakudoObject exceptionObject, RakudoObject exceptionType)
    {
        int wantType = Ops.unbox_int(tc, exceptionType);
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
            if (curContext.StaticCodeObject != null)
            {
                Handler[] handlers = curContext.StaticCodeObject.Handlers;
                if (handlers != null)
                    for (int i = 0; i < handlers.length; i++)
                        if (handlers[i].Type == wantType)
                            return ExceptionDispatcher.CallHandler(tc,
                                handlers[i].HandleBlock, exceptionObject);
            }
            curContext = curContext.Outer;
        }
        ExceptionDispatcher.DieFromUnhandledException(tc, exceptionObject);
        return null; // Unreachable; above call exits always.
    }

    /// <summary>
    /// Makes the outer context of the provided block be set to the current
    /// context. See ControlFlow.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Block"></param>
    /// <returns></returns>
    public static RakudoObject capture_outer(ThreadContext tc, RakudoCodeRef.Instance block)
    {
        block.OuterForNextInvocation = tc.CurrentContext;
        return block;
    }

    /// <summary>
    /// Creates a clone of the given code object, and makes its outer context
    /// be set to the current context. See ControlFlow.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="Block"></param>
    /// <returns></returns>
    public static RakudoObject new_closure(ThreadContext tc, RakudoCodeRef.Instance block)
    {
        // Clone all but OuterForNextInvocation and SC (since it's a new
        // object and doesn't live in any SC yet).
        RakudoCodeRef.Instance newBlock = (RakudoCodeRef.Instance)block.getSTable().REPR.instance_of(tc,block.getSTable().WHAT);
        // XXX the above line works differently in the C# version, more
        // like the line below, but that got a compiler error: an enclosing instance ... is required
        // RakudoCodeRef.Instance newBlock = new RakudoCodeRef.Instance(block.getSTable());
        newBlock.Body = block.Body;
        newBlock.CurrentContext = block.CurrentContext;
        newBlock.Handlers = block.Handlers;
        newBlock.OuterBlock = block.OuterBlock;
        newBlock.Sig = block.Sig;
        newBlock.StaticLexPad = block.StaticLexPad;

        // Set the outer for next invocation and return the cloned block.
        newBlock.OuterForNextInvocation = tc.CurrentContext;
        return newBlock;
    }

    /// <summary>
    /// Loads a module (that is, some pre-compiled compilation unit that
    /// was compiled using NQP). Expects the path minus an extension
    /// (that is, the .class will be added). Returns what the body of the
    /// compilation unit evaluated to. See Library.cs
    /// </summary>
    /// <param name="TC"></param>
    /// <param name="Path"></param>
    /// <returns></returns>
    public static RakudoObject load_module(ThreadContext tc, RakudoObject path)
    {
        // Load the assembly and grab the first type in it.
        //var assembly = AppDomain.CurrentDomain.Load(Ops.unbox_str(tc, path));
        //var class = Assembly.GetTypes()[0];

        // Call the Load method, passing along the current thread context
        // and the setting to use with it. What's returned is what the main
        // body of the compilation unit evaluates to.
        //var method = class.GetMethod("Load", BindingFlags.NonPublic | BindingFlags.Static);
        if (true) // sneak by the "unreachable code" monster
            throw new UnsupportedOperationException("load_module NYI");
        return (RakudoObject)null;
        //return (RakudoObject)method.Invoke.Invoke(null, new Object[] { tc, tc.Domain.Setting });
    }
    /// <summary>
    /// Gets a lexical variable of the given name. See Variables.cs
    /// </summary>
    /// <param name="i"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_lex(ThreadContext tc, String name)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null) {
            if (curContext.LexPad.SlotMapping.containsKey(name))
            {
                int index = curContext.LexPad.SlotMapping.get(name);
                return curContext.LexPad.Storage[index];
            }
            curContext = curContext.Outer;
        }
        throw new UnsupportedOperationException("No variable " + name + " found in the lexical scope");
    }

    /// <summary>
    /// Gets a lexical variable of the given name, but skips the current
    /// scope. See Variables.cs
    /// </summary>
    /// <param name="tc"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_lex_skip_current(ThreadContext tc, String name)
    {
        Context curContext = tc.CurrentContext.Outer;
        while (curContext != null)
        {
            if (curContext.LexPad.SlotMapping.containsKey(name))
            {
                int index = curContext.LexPad.SlotMapping.get(name);
                return curContext.LexPad.Storage[index];
            }
            curContext = curContext.Outer;
        }
        throw new UnsupportedOperationException("No variable " + name + " found in the lexical scope");
    }

    /// <summary>
    /// Binds the given value to a lexical variable of the given name. See Variables.cs
    /// </summary>
    /// <param name="i"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject bind_lex(ThreadContext tc, String name, RakudoObject value)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
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
    /// Looks up a variable in the dynamic scope. See Variables.cs
    /// </summary>
    /// <param name="C"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject get_dynamic(ThreadContext tc, String name)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null) {
            if (curContext.LexPad.SlotMapping.containsKey(name))
            {
                int index = curContext.LexPad.SlotMapping.get(name);
                return curContext.LexPad.Storage[index];
            }
            curContext = curContext.Caller;
        }
        throw new UnsupportedOperationException("No variable " + name + " found in the dynamic scope");
    }

    /// <summary>
    /// Binds the given value to a variable in the dynamic scope. See Variables.cs
    /// </summary>
    /// <param name="C"></param>
    /// <param name="name"></param>
    /// <returns></returns>
    public static RakudoObject bind_dynamic(ThreadContext tc, String name, RakudoObject value)
    {
        Context curContext = tc.CurrentContext;
        while (curContext != null)
        {
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

}
