using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Provides helper methods for getting stuff into and out of captures,
    /// both native ones and user-level ones.
    /// </summary>
    public static class CaptureHelper
    {
        /// <summary>
        /// Cache of the native capture type object.
        /// </summary>
        internal static IRakudoObject CaptureTypeObject;

        /// <summary>
        /// Forms a capture from the provided argumetns.
        /// </summary>
        /// <param name="Args"></param>
        /// <returns></returns>
        public static IRakudoObject FormWith(params IRakudoObject[] Args)
        {
            var C = (P6capture.Instance)CaptureTypeObject.STable.REPR.instance_of(CaptureTypeObject);
            C.Positionals = Args;
            return C;
        }

        /// <summary>
        /// Get a positional argument from a capture.
        /// </summary>
        /// <param name="Capture"></param>
        /// <param name="Pos"></param>
        /// <returns></returns>
        public static IRakudoObject GetPositional(IRakudoObject Capture, int Pos)
        {
            if (Capture is P6capture.Instance)
            {
                var Possies = (Capture as P6capture.Instance).Positionals;
                if (Possies != null && Pos < Possies.Length)
                    return Possies[Pos];
                else
                    throw new InvalidOperationException("Not enough positional parameters");
            }
            else
            {
                throw new NotImplementedException("Can only deal with native captures at the moment");
            }
        }

        /// <summary>
        /// Gets a positional and tries to unbox it to the given type.
        /// XXX In the future, we can make it call a coercion too, if
        /// needed.
        /// </summary>
        /// <typeparam name="Type"></typeparam>
        /// <param name="Capture"></param>
        /// <param name="Pos"></param>
        /// <returns></returns>
        public static Type GetPositionalAs<Type>(IRakudoObject Capture, int Pos)
        {
            return Ops.unbox<Type>(GetPositional(Capture, Pos));
        }

        /// <summary>
        /// XXX This is very wrong...
        /// </summary>
        /// <returns></returns>
        public static IRakudoObject Nil()
        {
            return null;
        }
    }
}
