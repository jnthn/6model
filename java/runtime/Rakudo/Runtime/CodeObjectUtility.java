package Rakudo.Runtime;

import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.REPRRegistry;
import Rakudo.Metamodel.Representation;
import Rakudo.Metamodel.Representations.RakudoCodeRef;

/// <summary>
/// This contains various utility methods that let us wrap up .Net
/// methods into static method info.
/// </summary>
public final class CodeObjectUtility
// public static class CodeObjectUtility // the C# version
{
    /// <summary>
    /// Cache of the low level code type object.
    /// </summary>
    public static RakudoCodeRef.Instance LLCodeTypeObject;

    /// <summary>
    /// This creates a wrapper object around a native method. We'll
    /// have to fix their HOWs up later. We do this hack by giving
    /// each one an STable with a dispatcher of their own that only
    /// supports forwarding to the native code.
    /// </summary>
    /// <param name="Code"></param>
    public static RakudoObject WrapNativeMethod(RakudoCodeRef.IFunc_Body code)
    {
        Representation repr = REPRRegistry.get_REPR_by_name("KnowHOWREPR");
        RakudoObject wrapper = repr.type_object_for(null);
        wrapper.getSTable().Invoke = code;
        return wrapper;
    }

    /// <summary>
    /// This is a helper to build a bunch of static block information.
    /// </summary>
    /// <param name="Code"></param>
    /// <returns></returns>
    public static RakudoCodeRef.Instance BuildStaticBlockInfo
    (
        RakudoCodeRef.IFunc_Body code,
        RakudoCodeRef.Instance outer,
        String[] lexNames
    )
    {
        // Create code wrapper object.
        RakudoCodeRef.Instance result = (RakudoCodeRef.Instance)LLCodeTypeObject.getSTable().REPR.instance_of(LLCodeTypeObject);
        
        // Put body, outer and signature in place.
        result.Body = code;
        result.OuterBlock = outer;

        // Setup static lexpad.
        result.StaticLexPad = new Lexpad(lexNames);

        return result;
    }
}

