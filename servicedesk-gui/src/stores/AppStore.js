import {create} from 'zustand'

export const useAppStore = create(set => ({
    user: {},
    controlCode: {},
    flash: {msg: "", className: "hide", type: "info"},
    setFlash: (message, type) => {
        set({flash: {msg: message, type: type || "info"}});
        if (!type || type === "info") {
            setTimeout(() => set({flash: {}}), 5000);
        }
    },
    clearFlash: () => set({flash: {}}),
    //[{path: "/roles/4", value: role.name}]
    breadcrumbPath: []
}));
