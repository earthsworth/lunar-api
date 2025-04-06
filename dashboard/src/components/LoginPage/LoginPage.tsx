import image from '../../assets/login-image.webp';
import {Button, Input} from "@headlessui/react";
import {FormEvent, useState} from "react";
import { motion, AnimatePresence } from "framer-motion";
import {Link} from "react-router-dom";

export function LoginPage() {
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const processLogin = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    // todo login
  }

  return (
    <div className="min-h-screen flex">
      <div className="hidden lg:flex w-1/2 items-center justify-center bg-base-200">
        <img
          src={image}
          alt="Image"
          className="w-full h-full object-cover"
        />
      </div>


      <div className="flex w-full lg:w-1/2 items-center justify-center px-8">
        <div className="w-full max-w-md space-y-6">
          <h1 className="text-3xl font-bold text-center select-none">The Lunar Dashboard</h1>

          <AnimatePresence>
            {error && (
              <motion.div
                className="alert alert-error shadow-sm text-sm"
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
                {error}
              </motion.div>
            )}
          </AnimatePresence>

          <form className="space-y-4 w-full" onSubmit={processLogin}>
            <label className="floating-label">
              <span>Minecraft Username</span>
              <Input type="text" placeholder="Minecraft Username" pattern="^[a-zA-Z0-9_]{3,16}$"
                     className="input validator input-md w-full" required/>
            </label>

            <label className="floating-label">
              <span>Password</span>
              <Input type="password" placeholder="Password" className="input input-md w-full" required/>
            </label>

            <div className="form-control mt-6">
              <Button type="submit" className="btn btn-primary w-full" disabled={loading}>
                {loading && <span className="loading loading-spinner"></span>}
                {loading ? "Loading" : "Login"}
              </Button>
            </div>
          </form>

          <div className="text-right select-none">
            <p className=" text-sm">
              Do not input your Microsoft password
            </p>
            <p className="text-red-500 font-semibold">.passwd &lt;new password&gt;</p>
            <Link to="/statistics" className="text-blue-500 dark:text-sky-500 underline hover:dark:text-blue-500 hover:text-sky-500">View Statistics</Link>
          </div>
        </div>
      </div>
    </div>
  );
}