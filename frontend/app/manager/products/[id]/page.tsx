"use client";

import { useEffect, useState, useRef } from "react";
import { useRouter, useParams } from "next/navigation";
import { apiGet, apiPatch } from "@/lib/api";
import axios from "axios";

interface Category {
  id: string;
  name: string;
}

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: string;
  imageUrl?: string;
}

interface UpdateProductForm {
  name?: string;
  description?: string;
  price?: string;
  stockQuantity?: string;
  categoryId?: string;
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
  "image/png": (b) =>
    b[0] === 0x89 && b[1] === 0x50 && b[2] === 0x4e && b[3] === 0x47,
  "image/webp": (b) =>
    b[0] === 0x52 && b[1] === 0x49 && b[2] === 0x46 && b[3] === 0x46,
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
      URL.revokeObjectURL(url);
      resolve(
        img.width <= MAX_IMAGE_DIMENSION && img.height <= MAX_IMAGE_DIMENSION,
      );
    };
    img.onerror = () => {
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

type PageStatus = "loading" | "idle" | "submitting" | "success" | "error";

function getErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message ?? "Request could not be processed.";
  }
  return "Request could not be processed.";
}

function validateForm(
  form: UpdateProductForm,
  _image: File | null,
): ProductFormErrors {
  const errors: ProductFormErrors = {};

  if (form.name !== undefined && form.name !== "") {
    if (form.name.length < 2 || form.name.length > 100) {
      errors.name = "Name must be between 2 and 100 characters.";
    }
  }

  if (form.description !== undefined && form.description !== "") {
    if (form.description.length < 10 || form.description.length > 1000) {
      errors.description =
        "Description must be between 10 and 1000 characters.";
    }
  }

  if (form.price !== undefined && form.price !== "") {
    const price = parseFloat(form.price);
    if (isNaN(price) || price < 0) {
      errors.price = "Price must be a positive value.";
    } else if (price > 999999.99) {
      errors.price = "Price must be less than or equal to 999,999.99.";
    }
  }

  if (form.stockQuantity !== undefined && form.stockQuantity !== "") {
    const stock = parseInt(form.stockQuantity);
    if (isNaN(stock) || stock < 0) {
      errors.stockQuantity = "Stock quantity must be a positive value.";
    } else if (stock > 999999) {
      errors.stockQuantity =
        "Stock quantity must be less than or equal to 999,999.";
    }
  }

  return errors;
}

export default function EditProductPage() {
  const router = useRouter();
  const params = useParams();
  const productId = params.id as string;
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [categories, setCategories] = useState<Category[]>([]);
  const [product, setProduct] = useState<Product | null>(null);
  const [pageStatus, setPageStatus] = useState<PageStatus>("loading");
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [form, setForm] = useState<UpdateProductForm>({});
  const [formErrors, setFormErrors] = useState<ProductFormErrors>({});
  const [image, setImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [hasChanges, setHasChanges] = useState(false);

  // Load product and categories
  useEffect(() => {
    const loadData = async () => {
      try {
        const [productData, categoriesData] = await Promise.all([
          apiGet<Product>(`/products/${productId}`),
          apiGet<Category[]>("/categories"),
        ]);
        setProduct(productData);
        setCategories(categoriesData);
        setPageStatus("idle");
      } catch (err) {
        setSubmitError("Failed to load product. Please try again.");
        setPageStatus("error");
      }
    };
    loadData();
  }, [productId]);

  const handleFieldChange = (field: keyof UpdateProductForm, value: string) => {
    setForm((prev) => {
      const updated = { ...prev, [field]: value };
      // Only set if different from original
      if (product && product[field as keyof Product]?.toString() === value) {
        const copy = { ...updated };
        delete copy[field];
        return copy;
      }
      return updated;
    });
    setHasChanges(true);
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

    if (!hasAllowedExtension(file.name)) {
      setFormErrors((prev) => ({
        ...prev,
        image: "File extension not allowed. Use JPEG, PNG, or WebP.",
      }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    if (!ALLOWED_MIME_TYPES.includes(file.type)) {
      setFormErrors((prev) => ({
        ...prev,
        image: "File type not allowed. Use JPEG, PNG, or WebP.",
      }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      setFormErrors((prev) => ({
        ...prev,
        image: "Image must be smaller than 5MB.",
      }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    const validMagic = await validateMagicBytes(file);
    if (!validMagic) {
      setFormErrors((prev) => ({
        ...prev,
        image:
          "File content does not match its extension. Please upload a valid image.",
      }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    const validDimensions = await validateImageDimensions(file);
    if (!validDimensions) {
      setFormErrors((prev) => ({
        ...prev,
        image: `Image dimensions must not exceed ${MAX_IMAGE_DIMENSION}×${MAX_IMAGE_DIMENSION}px.`,
      }));
      if (fileInputRef.current) fileInputRef.current.value = "";
      return;
    }

    setImage(file);
    setHasChanges(true);
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
    // Check if there are any changes
    if (!hasChanges && !image) {
      setSubmitError("No changes to save.");
      return;
    }

    const errors = validateForm(form, image);
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }

    try {
      setPageStatus("submitting");
      setSubmitError(null);

      const formData = new FormData();

      // Only include changed fields
      if (form.name !== undefined) formData.append("name", form.name);
      if (form.description !== undefined)
        formData.append("description", form.description);
      if (form.price !== undefined) formData.append("price", form.price);
      if (form.stockQuantity !== undefined)
        formData.append("stockQuantity", form.stockQuantity);
      if (form.categoryId !== undefined)
        formData.append("categoryId", form.categoryId);

      if (image) {
        formData.append("image", image);
      }

      const updatedProduct = await apiPatch<Product>(
        `/products/${productId}`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        },
      );

      setProduct(updatedProduct);
      setForm({});
      setImage(null);
      setImagePreview(null);
      setHasChanges(false);
      setSuccessMessage("Product updated successfully!");
      setPageStatus("idle");

      // Clear success message after 3 seconds
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setSubmitError(getErrorMessage(err));
      setPageStatus("idle");
    }
  };

  if (pageStatus === "loading") {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block w-8 h-8 border-4 border-slate-700 border-t-blue-500 rounded-full animate-spin mb-3"></div>
          <p className="text-slate-400">Loading product...</p>
        </div>
      </div>
    );
  }

  if (pageStatus === "error" || !product) {
    return (
      <div className="py-8">
        <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="min-h-[60vh] flex items-center justify-center">
            <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700 text-center">
              <svg
                className="w-16 h-16 text-red-400 mx-auto mb-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={1.5}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M12 9v3.75m-9.303 3.376c.865 1.728 2.883 2.875 5.303 2.875 2.42 0 4.438-1.147 5.303-2.875M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"
                />
              </svg>
              <h1 className="text-2xl font-bold text-white mb-2">
                Product not found
              </h1>
              <p className="text-slate-400 text-sm mb-8">{submitError}</p>
              <button
                onClick={() => router.push("/manager/products")}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition"
              >
                Back to Products
              </button>
            </div>
          </div>
        </main>
      </div>
    );
  }

  const isSubmitting = pageStatus === "submitting";
  const currentImageUrl = imagePreview || product.imageUrl;

  return (
    <div className="py-8">
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => router.push("/manager/products")}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition text-sm mb-4"
          >
            <svg
              className="w-4 h-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M15.75 19.5L8.25 12l7.5-7.5"
              />
            </svg>
            Back to Products
          </button>
          <h1 className="text-4xl font-bold text-white mb-1">Edit Product</h1>
          <p className="text-slate-400">
            Update product information. Only changed fields will be saved.
          </p>
        </div>

        <div className="bg-slate-800 rounded-xl border border-slate-700 p-6 space-y-6">
          {/* Success message */}
          {successMessage && (
            <div className="p-3 bg-green-900/60 border border-green-700 rounded text-green-200 text-sm flex items-center gap-2">
              <svg
                className="w-5 h-5 flex-shrink-0"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M5 13l4 4L19 7"
                />
              </svg>
              {successMessage}
            </div>
          )}

          {/* Image upload */}
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">
              Product Image{" "}
              <span className="text-slate-500 font-normal">(optional)</span>
            </label>
            {currentImageUrl ? (
              <div className="relative w-full h-48 rounded-lg overflow-hidden border border-slate-600 bg-slate-900">
                <img
                  src={currentImageUrl}
                  alt="Product"
                  className="h-full w-full object-cover"
                />
                {image && (
                  <div className="absolute inset-0 bg-blue-500/10 border-2 border-blue-400 rounded-lg" />
                )}
                {image && (
                  <button
                    onClick={handleRemoveImage}
                    className="absolute top-2 right-2 p-1.5 bg-slate-900/80 text-slate-300 rounded-full hover:text-white hover:bg-slate-900 transition"
                  >
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      strokeWidth={2}
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        d="M6 18L18 6M6 6l12 12"
                      />
                    </svg>
                  </button>
                )}
              </div>
            ) : (
              <div
                onClick={() => fileInputRef.current?.click()}
                className="w-full h-36 rounded-lg border-2 border-dashed border-slate-600 hover:border-blue-500 bg-slate-900 flex flex-col items-center justify-center gap-2 cursor-pointer transition group"
              >
                <svg
                  className="w-8 h-8 text-slate-500 group-hover:text-blue-400 transition"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  strokeWidth={1.5}
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 0 0 1.5-1.5V6a1.5 1.5 0 0 0-1.5-1.5H3.75A1.5 1.5 0 0 0 2.25 6v12a1.5 1.5 0 0 0 1.5 1.5Zm10.5-11.25h.008v.008h-.008V8.25Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z"
                  />
                </svg>
                <p className="text-sm text-slate-500 group-hover:text-slate-300 transition">
                  Click to update image
                </p>
                <p className="text-xs text-slate-600">
                  JPEG, PNG, WebP — max 5MB
                </p>
              </div>
            )}
            <input
              ref={fileInputRef}
              type="file"
              accept=".jpg,.jpeg,.png,.webp"
              onChange={handleImageChange}
              className="hidden"
            />
            {formErrors.image && (
              <p className="mt-1.5 text-xs text-red-400">{formErrors.image}</p>
            )}
          </div>

          <div className="border-t border-slate-700" />

          {/* Name */}
          <FormField label="Product Name" error={formErrors.name}>
            <input
              type="text"
              value={form.name ?? product.name}
              onChange={(e) => handleFieldChange("name", e.target.value)}
              disabled={isSubmitting}
              placeholder="Product name"
              className={inputClass(!!formErrors.name)}
            />
            <p className="mt-1 text-xs text-slate-500 text-right">
              {(form.name ?? product.name).length}/100
            </p>
          </FormField>

          {/* Description */}
          <FormField label="Description" error={formErrors.description}>
            <textarea
              value={form.description ?? product.description}
              onChange={(e) => handleFieldChange("description", e.target.value)}
              disabled={isSubmitting}
              placeholder="Product description"
              rows={4}
              className={inputClass(!!formErrors.description) + " resize-none"}
            />
            <p className="mt-1 text-xs text-slate-500 text-right">
              {(form.description ?? product.description).length}/1000
            </p>
          </FormField>

          {/* Price + Stock */}
          <div className="grid grid-cols-2 gap-4">
            <FormField label="Price (€)" error={formErrors.price}>
              <input
                type="number"
                min="0"
                max="999999.99"
                step="0.01"
                value={form.price ?? product.price}
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
                value={form.stockQuantity ?? product.stockQuantity}
                onChange={(e) =>
                  handleFieldChange("stockQuantity", e.target.value)
                }
                disabled={isSubmitting}
                placeholder="0"
                className={inputClass(!!formErrors.stockQuantity)}
              />
            </FormField>
          </div>

          {/* Category */}
          <FormField label="Category" error={formErrors.categoryId}>
            <select
              value={form.categoryId ?? product.categoryId}
              onChange={(e) => handleFieldChange("categoryId", e.target.value)}
              disabled={isSubmitting || categories.length === 0}
              className={
                inputClass(!!formErrors.categoryId) + " cursor-pointer"
              }
            >
              <option value="">Select a category...</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
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
              onClick={() => router.push("/manager/products")}
              disabled={isSubmitting}
              className="flex-1 px-6 py-3 bg-slate-700 text-white rounded font-medium hover:bg-slate-600 transition disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              disabled={isSubmitting || !hasChanges}
              className="flex-1 px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold disabled:opacity-50 hover:from-blue-600 hover:to-blue-700 transition flex items-center justify-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Saving...
                </>
              ) : (
                "Save Changes"
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
    hasError
      ? "border-red-500 focus:ring-red-500"
      : "border-slate-600 focus:ring-blue-500"
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
      <label className="block text-sm font-medium text-slate-300 mb-1.5">
        {label}
      </label>
      {children}
      {error && <p className="mt-1.5 text-xs text-red-400">{error}</p>}
    </div>
  );
}
