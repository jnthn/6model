using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;
using Rakudo.Metamodel.KnowHOW;
using Rakudo.Runtime;

namespace Rakudo
{
    /// <summary>
    /// Does initialization of the Rakudo library.
    /// </summary>
    public static class Init
    {
        /// <summary>
        /// Handles the various bits of initialization that are needed.
        /// Probably needs some don't-dupe-this work.
        /// </summary>
        public static ThreadContext Initialize()
        {
            // Bootstrap the meta-model.
            RegisterRepresentations();
            var KnowHOW = KnowHOWBootstrapper.Bootstrap();

            // Make a fake setting.
            // XXX Yes, this really is just a hack to get us started. :-)
            var Context = new Context();
            Context.LexPad = new Dictionary<string, IRakudoObject>()
            {
                { "KnowHOW", KnowHOW },
                { "print", CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                    {
                        Console.Write(CaptureHelper.GetPositionalAs<string>(C, 0));
                        return CaptureHelper.Nil();
                    })
                },
                { "say", CodeObjectUtility.WrapNativeMethod((TC, self, C) =>
                    {
                        Console.WriteLine(CaptureHelper.GetPositionalAs<string>(C, 0));
                        return CaptureHelper.Nil();
                    })
                },
                { "capture", REPRRegistry.get_REPR_by_name("P6capture").type_object_for(null) },
                { "Str", REPRRegistry.get_REPR_by_name("P6str").type_object_for(null) },
                { "Int", REPRRegistry.get_REPR_by_name("P6int").type_object_for(null) },
                { "Num", REPRRegistry.get_REPR_by_name("P6num").type_object_for(null) },
                { "LLCode", REPRRegistry.get_REPR_by_name("RakudoCodeRef").type_object_for(null) },
            };

            // Cache native capture and LLCode type object.
            CaptureHelper.CaptureTypeObject = Context.LexPad["capture"];
            CodeObjectUtility.LLCodeTypeObject = (RakudoCodeRef.Instance)Context.LexPad["LLCode"];

            // Create an execution domain and a thread context for it
            // and return that.
            var ExecDom = new ExecutionDomain();
            ExecDom.DefaultStrBoxType = Context.LexPad["Str"];
            var Thread = new ThreadContext();
            Thread.Domain = ExecDom;
            Thread.CurrentContext = Context;
            return Thread;
        }
        
        /// <summary>
        /// Registers all of the built-in representations.
        /// </summary>
        private static void RegisterRepresentations()
        {
            REPRRegistry.register_REPR("KnowHOWREPR", new KnowHOWREPR());
            REPRRegistry.register_REPR("P6opaque", new P6opaque());
            REPRRegistry.register_REPR("P6hash", new P6hash());
            REPRRegistry.register_REPR("P6int", new P6native<int>());
            REPRRegistry.register_REPR("P6num", new P6native<double>());
            REPRRegistry.register_REPR("P6str", new P6str());
            REPRRegistry.register_REPR("P6capture", new P6capture());
            REPRRegistry.register_REPR("RakudoCodeRef", new RakudoCodeRef());
        }
    }
}
