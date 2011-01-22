using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using Rakudo.Metamodel;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Ops relating to library loading.
    /// </summary>
    public static partial class Ops
    {
        /// <summary>
        /// Loads a module (that is, some pre-compiled compilation unit that
        /// was compiled using NQP). Expects the path minus an extension
        /// (that is, the .dll will be added). Returns what the body of the
        /// compilation unit evaluated to.
        /// </summary>
        /// <param name="TC"></param>
        /// <param name="Path"></param>
        /// <returns></returns>
        public static RakudoObject load_module(ThreadContext TC, RakudoObject Path)
        {
            // Load the assembly and grab the first type in it.
            var Assembly = AppDomain.CurrentDomain.Load(Ops.unbox_str(TC, Path));
            var Class = Assembly.GetTypes()[0];

            // Call the Load method, passing along the current thread context
            // and the setting to use with it. What's returned is what the main
            // body of the compilation unit evaluates to.
            var Method = Class.GetMethod("Load", BindingFlags.NonPublic | BindingFlags.Static);
            return (RakudoObject)Method.Invoke(null, new object[] { TC, TC.Domain.Setting });
        }
    }
}
