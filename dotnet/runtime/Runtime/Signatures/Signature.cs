using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Runtime
{
    /// <summary>
    /// Represents a signature.
    /// </summary>
    public class Signature
    {
        /// <summary>
        /// Creates a new Signature object instance.
        /// </summary>
        /// <param name="Parameters"></param>
        public Signature(Parameter[] Parameters)
        {
            this.Parameters = Parameters;
        }

        /// <summary>
        /// Gets the number of positionals.
        /// </summary>
        /// <returns></returns>
        internal int NumPositionals()
        {
            int Num = 0;
            for (int i = 0; i < Parameters.Length; i++)
                if (Parameters[i].Flags == Parameter.POS_FLAG)
                    Num++;
                else if (Parameters[i].Flags == Parameter.OPTIONAL_FLAG)
                    Num++;
                else
                    break;
            return Num;
        }

        /// <summary>
        /// Gets the number of required positionals.
        /// </summary>
        /// <returns></returns>
        internal int NumRequiredPositionals()
        {
            int Num = 0;
            for (int i = 0; i < Parameters.Length; i++)
                if (Parameters[i].Flags == Parameter.POS_FLAG)
                    Num++;
                else
                    break;
            return Num;
        }

        /// <summary>
        /// Do we have a slurpy positional parameter?
        /// </summary>
        /// <returns></returns>
        internal bool HasSlurpyPositional()
        {
            for (int i = 0; i < Parameters.Length; i++)
                if (Parameters[i].Flags == Parameter.POS_SLURPY_FLAG)
                    return true;
            return false;
        }

        /// <summary>
        /// The parameters we have.
        /// </summary>
        public Parameter[] Parameters;
    }
}
