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
        internal static RakudoObject CaptureTypeObject;

        /// <summary>
        /// Empty capture former.
        /// </summary>
        /// <returns></returns>
        public static RakudoObject FormWith()
        {
            var C = (P6capture.Instance)CaptureTypeObject.STable.REPR.instance_of(null, CaptureTypeObject);
            return C;
        }

        /// <summary>
        /// Forms a capture from the provided positional arguments.
        /// </summary>
        /// <param name="Args"></param>
        /// <returns></returns>
        public static RakudoObject FormWith(RakudoObject[] PosArgs)
        {
            var C = (P6capture.Instance)CaptureTypeObject.STable.REPR.instance_of(null, CaptureTypeObject);
            C.Positionals = PosArgs;
            return C;
        }

        /// <summary>
        /// Forms a capture from the provided positional and named arguments.
        /// </summary>
        /// <param name="Args"></param>
        /// <returns></returns>
        public static RakudoObject FormWith(RakudoObject[] PosArgs, Dictionary<string, RakudoObject> NamedArgs)
        {
            var C = (P6capture.Instance)CaptureTypeObject.STable.REPR.instance_of(null, CaptureTypeObject);
            C.Positionals = PosArgs;
            C.Nameds = NamedArgs;
            return C;
        }

        /// <summary>
        /// Get a positional argument from a capture.
        /// </summary>
        /// <param name="Capture"></param>
        /// <param name="Pos"></param>
        /// <returns></returns>
        public static RakudoObject GetPositional(RakudoObject Capture, int Pos)
        {
            var NativeCapture = Capture as P6capture.Instance;
            if (NativeCapture != null)
            {
                var Possies = NativeCapture.Positionals;
                if (Possies != null && Pos < Possies.Length)
                    return Possies[Pos];
                else
                    return null;
            }
            else
            {
                throw new NotImplementedException("Can only deal with native captures at the moment");
            }
        }

        /// <summary>
        /// Number of positionals.
        /// </summary>
        /// <param name="Capture"></param>
        /// <returns></returns>
        public static int NumPositionals(RakudoObject Capture)
        {
            var NativeCapture = Capture as P6capture.Instance;
            if (NativeCapture != null)
            {
                var Possies = NativeCapture.Positionals;
                return Possies == null ? 0 : Possies.Length;
            }
            else
            {
                throw new NotImplementedException("Can only deal with native captures at the moment");
            }
        }

        /// <summary>
        /// Get a named argument from a capture.
        /// </summary>
        /// <param name="Capture"></param>
        /// <param name="Pos"></param>
        /// <returns></returns>
        public static RakudoObject GetNamed(RakudoObject Capture, string Name)
        {
            var NativeCapture = Capture as P6capture.Instance;
            if (NativeCapture != null)
            {
                var Nameds = NativeCapture.Nameds;
                if (Nameds != null && Nameds.ContainsKey(Name))
                    return Nameds[Name];
                else
                    return null;
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
        /// <param name="Capture"></param>
        /// <param name="Pos"></param>
        /// <returns></returns>
        public static string GetPositionalAsString(RakudoObject Capture, int Pos)
        {
            return Ops.unbox_str(null, GetPositional(Capture, Pos));
        }

        /// <summary>
        /// XXX This is very wrong...
        /// </summary>
        /// <returns></returns>
        public static RakudoObject Nil()
        {
            return null;
        }
    }
}
