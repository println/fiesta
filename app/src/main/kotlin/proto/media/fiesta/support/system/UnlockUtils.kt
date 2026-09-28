package proto.media.fiesta.support.system

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import android.widget.Toast
import eu.chainfire.libsuperuser.Shell

object UnlockUtils {

    @JvmStatic
    fun unlock(activity: Activity) {
        if (Shell.SU.available()) {
            Shell.SU.run("pm disable --user 0 com.google.android.gms/.phenotype.service.sync.PhenotypeConfigurator")
            Shell.SU.run("pm disable --user 0 com.google.android.gms/.phenotype.service.PhenotypeService")
            Shell.SU.run("chmod 777 /data/data/com.google.android.gms/databases/phenotype.db*")
            try {
                val sql = SQLiteDatabase.openDatabase(
                    "/data/data/com.google.android.gms/databases/phenotype.db", null, 0
                )
                if (sql != null) {
                    val cursor = sql.rawQuery(
                        "SELECT stringVal FROM Flags WHERE packageName=? AND name=?;",
                        arrayOf("com.google.android.gms.car", "app_white_list")
                    )
                    val packageNames = mutableSetOf<String>()
                    if (cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val stringVal = cursor.getString(cursor.getColumnIndex("stringVal"))
                            if (stringVal != null) {
                                packageNames.add(stringVal)
                            }
                        }
                    }
                    cursor.close()
                    packageNames.add(activity.applicationContext.packageName) // add myself
                    sql.execSQL("DELETE FROM Flags WHERE packageName=\"com.google.android.gms.car\" AND name=\"app_white_list\";")
                    val joinedPackageNames = TextUtils.join(",", packageNames)
                    sql.execSQL("INSERT INTO Flags (packageName, version, flagType, partitionId, user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", 209, 0, 0, \"\", \"app_white_list\", \"$joinedPackageNames\", 1);")
                    sql.execSQL("INSERT INTO Flags (packageName, version, flagType, partitionId, user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", 224, 0, 0, \"\", \"app_white_list\", \"$joinedPackageNames\", 1);")
                    sql.close()
                    Toast.makeText(activity, "Successfully unlocked. Reboot phone and connect to Android Auto", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(activity, "Error in executing commands : sql exception : $e", Toast.LENGTH_LONG).show()
            }
            Shell.SU.run("chmod 660 /data/data/com.google.android.gms/databases/phenotype.db*")
            return
        }
        Toast.makeText(activity, "Root not available, install SuperSU and perform root first.", Toast.LENGTH_LONG).show()
    }
}
