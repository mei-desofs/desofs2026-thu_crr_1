/**
 * V3.2: Safe DOM Manipulation Hooks
 * Prevents XSS and DOM clobbering attacks
 */

import { useEffect, useRef, useCallback } from 'react';

/**
 * V3.2.2: Hook for safely rendering HTML content
 * Uses createTextNode to prevent script injection
 */
export function useSafeTextContent(text: string | null) {
  const elementRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!elementRef.current || !text) return;

    // Clear previous content
    while (elementRef.current.firstChild) {
      elementRef.current.removeChild(elementRef.current.firstChild);
    }

    // V3.2.2: Use createTextNode for safe text rendering
    const textNode = document.createTextNode(text);
    elementRef.current.appendChild(textNode);
  }, [text]);

  return elementRef;
}

/**
 * V3.2.3: Hook to prevent DOM clobbering
 * Uses explicit variable declarations and strict type checking
 */
export function useSafeDOMAccess<T extends HTMLElement>(
  selector: string
): T | null {
  const elementRef = useRef<T | null>(null);

  useEffect(() => {
    const element = document.querySelector(selector) as T | null;

    // V3.2.3: Strict type checking and explicit variable declaration
    if (element !== null && element instanceof HTMLElement) {
      elementRef.current = element;
    } else {
      elementRef.current = null;
    }
  }, [selector]);

  return elementRef.current;
}

/**
 * V3.2.2: Hook for safe HTML content with sanitization
 */
export function useSafeHTML(html: string) {
  const elementRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!elementRef.current) return;

    // Create a temporary div to parse HTML safely
    const temp = document.createElement('div');
    temp.textContent = html; // Use textContent instead of innerHTML

    // Clear and replace content
    elementRef.current.innerHTML = '';
    while (temp.firstChild) {
      elementRef.current.appendChild(temp.firstChild);
    }
  }, [html]);

  return elementRef;
}

/**
 * V3.2.3: Hook to avoid storing global variables on document
 * Uses local state instead of window/document properties
 */
export function useSafeGlobalState<T>(_key: string, initialValue: T) {
  const stateRef = useRef<T>(initialValue);

  const setState = useCallback((value: T) => {
    stateRef.current = value;
  }, []);

  const getState = useCallback(() => {
    return stateRef.current;
  }, []);

  return { getState, setState };
}

/**
 * V3.2.3: Hook for namespace isolation
 * Creates isolated scope for variables to prevent DOM clobbering
 */
export function useNamespaceIsolation() {
  const namespaceRef = useRef<Record<string, unknown>>({});

  const setNamespaceValue = useCallback(
    (key: string, value: unknown) => {
      namespaceRef.current[key] = value;
    },
    []
  );

  const getNamespaceValue = useCallback(
    (key: string): unknown => {
      return namespaceRef.current[key];
    },
    []
  );

  const clearNamespace = useCallback(() => {
    namespaceRef.current = {};
  }, []);

  return { setNamespaceValue, getNamespaceValue, clearNamespace };
}

/**
 * V3.5.5: Hook for safe postMessage handling
 * Validates origin before processing messages
 */
export function usePostMessageListener(
  trustedOrigins: string[],
  callback: (data: unknown) => void
) {
  useEffect(() => {
    const handleMessage = (event: MessageEvent) => {
      // V3.5.5: Always validate origin
      if (!trustedOrigins.includes(event.origin)) {
        console.warn(`Ignoring message from untrusted origin: ${event.origin}`);
        return;
      }

      // Validate message syntax
      if (typeof event.data !== 'object' || event.data === null) {
        console.warn('Invalid message format');
        return;
      }

      callback(event.data);
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [trustedOrigins, callback]);
}

/**
 * V3.7.3: Hook for external link redirection warning
 * Warns user before redirecting to external URLs
 */
export function useExternalLinkWarning() {
  const shouldRedirect = useCallback((url: string): boolean => {
    // Check if URL is external
    const currentOrigin = window.location.origin;
    const urlOrigin = new URL(url, window.location.href).origin;

    if (urlOrigin !== currentOrigin) {
      const confirmed = window.confirm(
        `You are about to be redirected to an external website:\n\n${urlOrigin}\n\nDo you want to continue?`
      );
      return confirmed;
    }

    return true;
  }, []);

  return { shouldRedirect };
}
