#!/usr/bin/env python3
"""
Fix para el bug de Collections.sort() con CopyOnWriteArrayList en Android 7
Uso: python3 fix_android_sort_bug.py
"""

import os
import re
import shutil
from pathlib import Path
from datetime import datetime

# Configuración
PROJECT_ROOT = Path.cwd()
APP_SRC = PROJECT_ROOT / "app" / "src" / "main" / "java" / "com" / "apk" / "editor"
BACKUP_DIR = PROJECT_ROOT / "backups" / datetime.now().strftime("%Y%m%d_%H%M%S")

# Archivos a modificar y sus cambios específicos
FILES_TO_FIX = [
    "fragments/APKExplorerFragment.java",
    "utils/APKData.java",
    "utils/APKExplorer.java",
    "utils/AppData.java",
    "utils/Projects.java",
]

# Contenido del helper
HELPER_CONTENT = '''package com.apk.editor.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Helper para evitar el bug de Collections.sort() con CopyOnWriteArrayList en Android 7
 * Ver: https://issuetracker.google.com/issues/37032533
 */
public class SafeSortHelper {
    
    /**
     * Ordena una lista con comparador personalizado
     */
    public static <T> void safeSort(List<T> list, Comparator<? super T> comparator) {
        if (list == null || list.size() <= 1) return;
        
        if (list instanceof CopyOnWriteArrayList) {
            // Copiar a ArrayList temporal para evitar UnsupportedOperationException
            ArrayList<T> temp = new ArrayList<>(list);
            temp.sort(comparator);
            list.clear();
            list.addAll(temp);
        } else {
            list.sort(comparator);
        }
    }
    
    /**
     * Ordena una lista con orden natural (Comparable)
     */
    public static <T extends Comparable<? super T>> void safeSort(List<T> list) {
        if (list == null || list.size() <= 1) return;
        
        if (list instanceof CopyOnWriteArrayList) {
            ArrayList<T> temp = new ArrayList<>(list);
            Collections.sort(temp);
            list.clear();
            list.addAll(temp);
        } else {
            Collections.sort(list);
        }
    }
}
'''

def create_backup(file_path):
    """Crea un backup del archivo original"""
    try:
        # Obtener la ruta relativa desde APP_SRC
        rel_path = file_path.relative_to(APP_SRC)
        backup_path = BACKUP_DIR / rel_path
        backup_path.parent.mkdir(parents=True, exist_ok=True)
        
        # Verificar que el archivo existe
        if not file_path.exists():
            print(f"  ⚠️  Archivo no encontrado: {file_path}")
            return False
            
        shutil.copy2(file_path, backup_path)
        print(f"  ✅ Backup creado: {backup_path}")
        return True
    except Exception as e:
        print(f"  ❌ Error al crear backup: {e}")
        return False

def add_import(content, import_statement):
    """Agrega un import si no existe"""
    if import_statement in content:
        return content
    
    lines = content.split('\n')
    
    # Buscar el último import existente
    last_import_idx = -1
    for i, line in enumerate(lines):
        if line.startswith('import '):
            last_import_idx = i
    
    if last_import_idx >= 0:
        lines.insert(last_import_idx + 1, import_statement)
    else:
        # Si no hay imports, buscar package
        for i, line in enumerate(lines):
            if line.startswith('package '):
                lines.insert(i + 1, '')
                lines.insert(i + 2, import_statement)
                break
        else:
            # Si no hay package, insertar al inicio
            lines.insert(0, import_statement)
            lines.insert(1, '')
    
    return '\n'.join(lines)

def fix_collections_sort(content):
    """Reemplaza Collections.sort() por SafeSortHelper.safeSort()"""
    # Patrón más preciso para encontrar Collections.sort
    pattern = r'Collections\.sort\s*\(([^;]+?)\s*\)'
    
    def replace_match(match):
        args = match.group(1).strip()
        return f'SafeSortHelper.safeSort({args})'
    
    new_content = re.sub(pattern, replace_match, content)
    return new_content

def fix_file(file_path):
    """Procesa un archivo completo"""
    print(f"\n📝 Procesando: {file_path.name}")
    
    if not file_path.exists():
        print(f"  ⚠️  Archivo no encontrado, saltando")
        return False
    
    # Leer contenido original
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            original_content = f.read()
    except Exception as e:
        print(f"  ❌ Error al leer archivo: {e}")
        return False
    
    # Verificar si tiene Collections.sort
    if 'Collections.sort(' not in original_content:
        print(f"  ⚠️  No contiene Collections.sort, saltando")
        return True
    
    # Crear backup
    if not create_backup(file_path):
        return False
    
    # Aplicar cambios
    modified_content = original_content
    
    # 1. Agregar import si no existe
    import_stmt = 'import com.apk.editor.utils.SafeSortHelper;'
    if import_stmt not in modified_content:
        modified_content = add_import(modified_content, import_stmt)
        print(f"  ✅ Import agregado")
    
    # 2. Reemplazar Collections.sort
    modified_content = fix_collections_sort(modified_content)
    
    # Verificar si hubo cambios
    if modified_content == original_content:
        print(f"  ⚠️  Sin cambios necesarios")
        return True
    
    # Guardar archivo modificado
    try:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(modified_content)
        print(f"  ✅ Archivo parcheado correctamente")
        return True
    except Exception as e:
        print(f"  ❌ Error al guardar archivo: {e}")
        return False

def create_helper_file():
    """Crea el archivo SafeSortHelper.java"""
    helper_path = APP_SRC / "utils" / "SafeSortHelper.java"
    helper_path.parent.mkdir(parents=True, exist_ok=True)
    
    if helper_path.exists():
        print(f"\n📝 SafeSortHelper.java ya existe, verificando...")
        with open(helper_path, 'r', encoding='utf-8') as f:
            existing = f.read()
        if "SafeSortHelper" in existing and "CopyOnWriteArrayList" in existing:
            print("  ✅ Ya existe y es válido")
            return True
    
    print(f"\n📝 Creando SafeSortHelper.java...")
    try:
        with open(helper_path, 'w', encoding='utf-8') as f:
            f.write(HELPER_CONTENT)
        print("  ✅ Creado correctamente")
        return True
    except Exception as e:
        print(f"  ❌ Error al crear: {e}")
        return False

def verify_fix():
    """Verifica que los cambios se aplicaron correctamente"""
    print("\n🔍 Verificando cambios...")
    
    issues = []
    for file_rel in FILES_TO_FIX:
        full_path = APP_SRC / file_rel
        if not full_path.exists():
            continue
            
        with open(full_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Verificar que no queden Collections.sort sin modificar
        if 'Collections.sort(' in content and 'SafeSortHelper' not in content:
            issues.append(f"  ⚠️  {file_rel}: aún tiene Collections.sort sin SafeSortHelper")
        
        # Verificar que tenga el import
        if 'import com.apk.editor.utils.SafeSortHelper' not in content and 'Collections.sort(' in content:
            issues.append(f"  ⚠️  {file_rel}: falta el import de SafeSortHelper")
    
    if issues:
        print("\n❌ Problemas encontrados:")
        for issue in issues:
            print(issue)
        return False
    else:
        print("✅ Todos los archivos están correctamente parcheados")
        return True

def main():
    """Función principal"""
    print("=" * 60)
    print("🔧 Fix para Collections.sort() en Android 7")
    print("=" * 60)
    
    # Verificar que estamos en el directorio correcto
    if not (APP_SRC / "utils").exists():
        print(f"❌ Error: No se encuentra el directorio {APP_SRC}")
        print(f"   Directorio actual: {PROJECT_ROOT}")
        print("   Asegúrate de ejecutar el script desde la raíz del proyecto APK-Explorer-Editor")
        return False
    
    # Crear directorio de backups
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    print(f"\n📁 Backups guardados en: {BACKUP_DIR}")
    
    # Crear archivo helper
    if not create_helper_file():
        return False
    
    # Procesar cada archivo
    print("\n📂 Procesando archivos...")
    success_count = 0
    for file_rel in FILES_TO_FIX:
        full_path = APP_SRC / file_rel
        if fix_file(full_path):
            success_count += 1
    
    print(f"\n📊 Resumen: {success_count}/{len(FILES_TO_FIX)} archivos procesados")
    
    # Verificar cambios
    verify_fix()
    
    print("\n" + "=" * 60)
    print("✅ Proceso completado!")
    print("📝 Prueba la app en Android 7 para verificar que el crash se ha solucionado")
    print("💾 Los backups están en:", BACKUP_DIR)
    print("=" * 60)
    
    return True

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
        exit(1)
