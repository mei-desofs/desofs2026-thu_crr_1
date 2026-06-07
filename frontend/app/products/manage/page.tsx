"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { apiGet, apiPost } from "@/lib/api";
import axios from "axios";

interface Category {
  id: string;
  name: string;
}

interface ProductForm {
  name: string;
  description: string;
  price: string;
  stockQuantity: string;
  categoryId: string;
}

interface ProductFormErrors {
  name?: string;
  description?: string;
  price?: string;
  stockQuantity?: string;
  categoryId?: string;
  image?: string;
}

// ─── Image validation constants ───────────────────────────────────────────────
const MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
const MAX_IMAGE_DIMENSION = 4096; // px — prevents pixel flood attacks
const ALLOWED_MIME_TYPES = ["image/jpeg", "image/png", "image/webp"];
const ALLOWED_EXTENSIONS = [".jpg", ".jpeg", ".png", ".webp"];

// Magic bytes for each allowed type
const MAGIC_BYTES: Record<string, (b: Uint8Array) => boolean> = {
  "image/jpeg": (b) => b[0] === 0xff && b[1] === 0xd8 && b[2] === 0xff,
  "image/png": (b) => b[0] === 0x89 && b[1] === 0x50 && b[2] === 0x4e && b[3] === 0x47,
  "image/webp": (b) => b[0] === 0x52 && b[1] === 0x49 && b[2] === 0x46 && b[3] === 0x46,
};

async function validateMagicBytes(file: File): Promise<boolean> {
  const buffer = await file.slice(0, 4).arrayBuffer();
  const bytes = new Uint8Array(buffer);
  const checker = MAGIC_BYTES[file.type];
  return checker ? checker(bytes) : false;
}

async function validateImageDimensions(file: File): Promise<boolean> {
  return new Promise((resolve) => {
    const img = new Image();
    const url = URL.createObjectURL(file);
    img.onload = () => {
      console.log("[ImageValidation] onload — width:", img.width, "height:", img.height, "max:", MAX_IMAGE_DIMENSION);
      URL.revokeObjectURL(url);
      resolve(img.width <= MAX_IMAGE_DIMENSION && img.height <= MAX_IMAGE_DIMENSION);
    };
    img.onerror = (e) => {
      console.log("[ImageValidation] onerror — blob URL blocked or file unreadable:", e);
      URL.revokeObjectURL(url);
      resolve(false);
    };
    img.src = url;
  });
}

function hasAllowedExtension(filename: string): boolean {
  const lower = filename.toLowerCase();
  return ALLOWED_EXTENSIONS.some((ext) => lower.endsWith(ext));
}

type PageStatus = "idle" | "loading" | "submitting" | "success" | "error";

function getErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message ?? "Request could not be processed.";
  }
  return "Request could not be processed.";
}

function validateForm(form: ProductForm, _image: File | null): ProductFormErrors {
  const errors: ProductFormErrors = {};

  if (!form.name.trim()) {
    errors.name = "Product name is required.";
  } else if (form.name.length < 2 || form.name.length > 100) {
    errors.name = "Name must be between 2 and 100 characters.";
  }

  if (!form.description.trim()) {
    errors.description = "Description is required.";
  } else if (form.description.length < 10 || form.description.length > 1000) {
    errors.description = "Description must be between 10 and 1000 characters.";
  }

  const price = parseFloat(form.price);
  if (!form.price.trim()) {
    errors.price = "Price is required.";
  } else if (isNaN(price) || price < 0) {
    errors.price = "Price must be a positive value.";
  } else if (price > 999999.99) {
    errors.price = "Price must be less than or equal to 999,999.99.";
  }

  const stock = parseInt(form.stockQuantity);
  if (!form.stockQuantity.trim()) {
    errors.stockQuantity = "Stock quantity is required.";
  } else if (isNaN(stock) || stock < 0) {
    errors.stockQuantity = "Stock quantity must be a positive value.";
  } else if (stock > 999999) {
    errors.stockQuantity = "Stock quantity must be less than or equal to 999,999.";
  }

  if (!form.categoryId) {
    errors.categoryId = "Please select a category.";
  }

  return errors;
}

export default function CreateProductPage() {
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [categories, setCategories] = useState<Category[]>([]);
  const [pageStatus, setPageStatus] = useState<PageStatus>("loading");
  const [submitError, setSubmitError] = useState<string | null>(null);

  const [form, setForm] = useState<ProductForm>({
    name: "",
    description: "",
    price: "",
    stockQuantity: "",
    categoryId: "",
  });
  const [formErrors, setFormErrors] = useState<ProductFormErrors>({});
  const [image, setImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);

  useEffect(() => {
    const loadCategories = async () => {
      try {
        const data = await apiGet<Category[]>("/categories");
        setCategories(data);
        setPageStatus("idle");
      } catch {
        setSubmitError("Failed to load categories. Please refresh.");
        setPageStatus("error");
      }
    };
    loadCategories();
  }, []);

  const handleFieldChange = (field: keyof ProductForm, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (formErrors[field]) {
      setFormErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] ?? null;
    setImage(null);
    setImagePreview(null);
    setFormErrors((prev) => ({ ...prev, image: undefined }));

    if (!file) return;

    // 1. Extension check
    if (!hasAllowedExtension(file.name)) {
      setFormErrors((prev) => ({ ...prev, image: "File extension not allowed. Use JPEG, PNG, or WebP." }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    // 2. MIME type check
    if (!ALLOWED_MIME_TYPES.includes(file.type)) {
      setFormErrors((prev) => ({ ...prev, image: "File type not allowed. Use JPEG, PNG, or WebP." }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    // 3. Size check
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      setFormErrors((prev) => ({ ...prev, image: "Image must be smaller than 5MB." }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    // 4. Magic bytes — confirms file content matches declared type
    const validMagic = await validateMagicBytes(file);
    if (!validMagic) {
      setFormErrors((prev) => ({ ...prev, image: "File content does not match its extension. Please upload a valid image." }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    // 5. Dimensions — prevents pixel flood attacks
    const validDimensions = await validateImageDimensions(file);
    if (!validDimensions) {
      setFormErrors((prev) => ({ ...prev, image: `Image dimensions must not exceed ${MAX_IMAGE_DIMENSION}×${MAX_IMAGE_DIMENSION}px.` }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    // All checks passed
    setImage(file);
    const reader = new FileReader();
    reader.onloadend = () => setImagePreview(reader.result as string);
    reader.readAsDataURL(file);
  };

  const handleRemoveImage = () => {
    setImage(null);
    setImagePreview(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleSubmit = async () => {
    const errors = validateForm(form, image);
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }

    try {
      setPageStatus("submitting");
      setSubmitError(null);

      // Submit product data as JSON (image support to be added when backend is ready)
      await apiPost("/products", {
        name: form.name.trim(),
        description: form.description.trim(),
        price: parseFloat(form.price),
        stockQuantity: parseInt(form.stockQuantity),
        categoryId: form.categoryId,
      });

      setPageStatus("success");
    } catch (err) {
      setSubmitError(getErrorMessage(err));
      setPageStatus("idle");
    }
  };

  // ── Success ──────────────────────────────────────────────────────────────
  if (pageStatus === "success") {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700 text-center">
          <div className="w-16 h-16 bg-green-900/50 rounded-full flex items-center justify-center mx-auto mb-5">
            <svg className="w-8 h-8 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">Product created!</h1>
          <p className="text-slate-400 text-sm mb-8">The product has been added to the catalogue.</p>
          <div className="flex gap-3">
            <button
              onClick={() => setPageStatus("idle")}
              className="flex-1 px-4 py-2.5 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
            >
              Add another
            </button>
            <button
              onClick={() => router.push("/manager/dashboard")}
              className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
            >
              Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  const isSubmitting = pageStatus === "submitting";

  return (
    <div className="py-8">
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => router.push("/manager/dashboard")}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition text-sm mb-4"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
            </svg>
            Back to Dashboard
          </button>
          <h1 className="text-4xl font-bold text-white mb-1">New Product</h1>
          <p className="text-slate-400">Add a new product to the catalogue.</p>
        </div>

        <div className="bg-slate-800 rounded-xl border border-slate-700 p-6 space-y-6">

          {/* Image upload */}
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Product Image <span className="text-slate-500 font-normal">(optional — backend support coming soon)</span>
            </label>
            {imagePreview ? (
              <div className="relative w-full h-48 rounded-lg overflow-hidden border border-slate-600 bg-slate-900">
                <img src={imagePreview} alt="Preview" className="w-full h-full object-cover" />
                <button
                  onClick={handleRemoveImage}
                  className="absolute top-2 right-2 p-1.5 bg-slate-900/80 text-slate-300 rounded-full hover:text-white hover:bg-slate-900 transition"
                >
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            ) : (
              <div
                onClick={() => fileInputRef.current?.click()}
                className="w-full h-36 rounded-lg border-2 border-dashed border-slate-600 hover:border-blue-500 bg-slate-900 flex flex-col items-center justify-center gap-2 cursor-pointer transition group"
              >
                <svg className="w-8 h-8 text-slate-500 group-hover:text-blue-400 transition" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 0 0 1.5-1.5V6a1.5 1.5 0 0 0-1.5-1.5H3.75A1.5 1.5 0 0 0 2.25 6v12a1.5 1.5 0 0 0 1.5 1.5Zm10.5-11.25h.008v.008h-.008V8.25Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z" />
                </svg>
                <p className="text-sm text-slate-500 group-hover:text-slate-300 transition">Click to upload image</p>
                <p className="text-xs text-slate-600">JPEG, PNG, WebP — max 5MB</p>
              </div>
            )}
            <input
              ref={fileInputRef}
              type="file"
              accept=".jpg,.jpeg,.png,.webp"
              onChange={handleImageChange}
              className="hidden"
            />
            {formErrors.image && <p className="mt-1.5 text-xs text-red-400">{formErrors.image}</p>}
          </div>

          <div className="border-t border-slate-700" />

          {/* Name */}
          <FormField
            label="Product Name"
            error={formErrors.name}
          >
            <input
              type="text"
              value={form.name}
              onChange={(e) => handleFieldChange("name", e.target.value)}
              disabled={isSubmitting}
              placeholder="e.g. Wireless Keyboard"
              className={inputClass(!!formErrors.name)}
            />
            <p className="mt-1 text-xs text-slate-500 text-right">{form.name.length}/100</p>
          </FormField>

          {/* Description */}
          <FormField label="Description" error={formErrors.description}>
            <textarea
              value={form.description}
              onChange={(e) => handleFieldChange("description", e.target.value)}
              disabled={isSubmitting}
              placeholder="Describe the product..."
              rows={4}
              className={inputClass(!!formErrors.description) + " resize-none"}
            />
            <p className="mt-1 text-xs text-slate-500 text-right">{form.description.length}/1000</p>
          </FormField>

          {/* Price + Stock */}
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Price (€)" error={formErrors.price}>
              <input
                type="number"
                min="0"
                max="999999.99"
                step="0.01"
                value={form.price}
                onChange={(e) => handleFieldChange("price", e.target.value)}
                disabled={isSubmitting}
                placeholder="0.00"
                className={inputClass(!!formErrors.price)}
              />
            </FormField>
            <FormField label="Stock Quantity" error={formErrors.stockQuantity}>
              <input
                type="number"
                min="0"
                max="999999"
                step="1"
                value={form.stockQuantity}
                onChange={(e) => handleFieldChange("stockQuantity", e.target.value)}
                disabled={isSubmitting}
                placeholder="0"
                className={inputClass(!!formErrors.stockQuantity)}
              />
            </FormField>
          </div>

          {/* Category */}
          <FormField label="Category" error={formErrors.categoryId}>
            <select
              value={form.categoryId}
              onChange={(e) => handleFieldChange("categoryId", e.target.value)}
              disabled={isSubmitting || categories.length === 0}
              className={inputClass(!!formErrors.categoryId) + " cursor-pointer"}
            >
              <option value="">Select a category...</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </FormField>

          {/* Submit error */}
          {submitError && (
            <div className="p-3 bg-red-900/60 border border-red-700 rounded text-red-200 text-sm">
              {submitError}
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <button
              onClick={() => router.push("/manager/dashboard")}
              disabled={isSubmitting}
              className="flex-1 px-6 py-3 bg-slate-700 text-white rounded font-medium hover:bg-slate-600 transition disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              disabled={isSubmitting || pageStatus === "loading"}
              className="flex-1 px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold disabled:opacity-50 hover:from-blue-600 hover:to-blue-700 transition flex items-center justify-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Creating...
                </>
              ) : (
                "Create Product"
              )}
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

function inputClass(hasError: boolean) {
  return `w-full px-3 py-2.5 rounded bg-slate-900 text-white placeholder-slate-500 border focus:outline-none focus:ring-2 focus:border-transparent transition text-sm disabled:opacity-50 ${
    hasError ? "border-red-500 focus:ring-red-500" : "border-slate-600 focus:ring-blue-500"
  }`;
}

interface FormFieldProps {
  label: string;
  error?: string;
  children: React.ReactNode;
}

function FormField({ label, error, children }: FormFieldProps) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>
      {children}
      {error && <p className="mt-1.5 text-xs text-red-400">{error}</p>}
    </div>
  );
}