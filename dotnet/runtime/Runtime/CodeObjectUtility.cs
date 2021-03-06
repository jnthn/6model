﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// This contains various utility methods that let us wrap up .Net
    /// methods into static method info.
    /// </summary>
    public static class CodeObjectUtility
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
        public static RakudoObject WrapNativeMethod(Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject> Code)
        {
            var REPR = REPRRegistry.get_REPR_by_name("KnowHOWREPR");
            var Wrapper = REPR.type_object_for(null, null);
            Wrapper.STable.SpecialInvoke = Code;
            return Wrapper;
        }

        /// <summary>
        /// This is a helper to build a bunch of static block information.
        /// </summary>
        /// <param name="Code"></param>
        /// <returns></returns>
        public static RakudoCodeRef.Instance BuildStaticBlockInfo(
            Func<ThreadContext, RakudoObject, RakudoObject, RakudoObject> Code,
            RakudoCodeRef.Instance Outer, string[] LexNames)
        {
            // Create code wrapper object.
            var Result = (RakudoCodeRef.Instance)LLCodeTypeObject.STable.REPR.instance_of(null, LLCodeTypeObject);
            
            // Put body, outer and handlers in place.
            Result.Body = Code;
            Result.OuterBlock = Outer;

            // Setup static lexpad.
            Result.StaticLexPad = new Lexpad(LexNames);

            return Result;
        }
    }
}
