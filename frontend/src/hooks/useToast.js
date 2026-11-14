import { useCallback, useEffect, useRef, useState } from 'react';

/** Dùng chung notification; huỷ timer cũ để toast mới không bị tắt quá sớm. */
export function useToast() {
  const [toast, setToast] = useState({ show: false, message: '', title: '', type: 'success' });
  const timer = useRef(null);
  useEffect(() => () => clearTimeout(timer.current), []);

  const triggerToast = useCallback((message, title = 'Thành công', type = 'success') => {
    clearTimeout(timer.current);
    setToast({ show: true, message, title, type });
    timer.current = setTimeout(() => setToast((previous) => ({ ...previous, show: false })), 2800);
  }, []);

  return { toast, triggerToast };
}
