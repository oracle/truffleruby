package org.truffleruby.core;

public class ReferenceProcessingService<T extends ReferenceProcessingService.ProcessingReference<T>> {

    public static interface ProcessingReference<U extends ProcessingReference<U>> {
        public U getPrevious();

        public void setPrevious(U previous);

        public U getNext();

        public void setNext(U next);
    }

    /** The head of a doubly-linked list of FinalizerReference, needed to collect finalizer Procs for ObjectSpace. */
    private T first = null;

    public ReferenceProcessingService() {
        super();
    }

    protected synchronized void remove(T ref) {
        if (ref.getNext() == ref) {
            // Already removed.
            return;
        }
    
        if (first == ref) {
            if (ref.getNext() != null) {
                first = ref.getNext();
            } else {
                // The list becomes empty
                first = null;
            }
        }
    
        if (ref.getNext() != null) {
            ref.getNext().setPrevious(ref.getPrevious());
        }
        if (ref.getPrevious() != null) {
            ref.getPrevious().setNext(ref.getNext());
        }
    
        // Mark that this ref has been removed.
        ref.setNext(ref);
        ref.setPrevious(ref);
    }

    protected synchronized void add(T newRef) {
        if (first != null) {
            newRef.setNext(first);
            first.setPrevious(newRef);
        }
        first = newRef;
    }

    protected T getFirst() {
        return first;
    }
}
