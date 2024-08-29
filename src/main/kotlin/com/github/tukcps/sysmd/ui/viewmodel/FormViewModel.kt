package com.github.tukcps.sysmd.ui.viewmodel

class FormCell(var value: String, var editable: Boolean = true)

class FormRow {
    val cells = mutableListOf<FormCell>()
}

class FormViewModel(private var rows: Int, private var cols: Int, val body: String, private val maskList: ArrayList<Boolean>) {

    var formRows = mutableListOf<FormRow>()
    val formColMaskList = mutableListOf<Boolean>()

    private fun initData() {
        //Setting up masks from File Text
        maskList.forEachIndexed { index, value ->
            if (formColMaskList.size > index) {
                formColMaskList[index] = value
            }
        }

        if (body.isNotEmpty()) {
            //Setting up cell content from File Text
            val rows = body.split(getFormRowDelimiter())
            rows.forEachIndexed { i, row ->
                if (formRows.size - 1 >= i) {
                    val cells = row.split(getFormColDelimiter())
                    cells.forEachIndexed { j, cell ->
                        if (formRows[i].cells.size - 1 >= j) {
                            formRows[i].cells[j].value = cell
                            formRows[i].cells[j].editable = formColMaskList[j]
                        }
                    }
                }
            }
        }
    }

    init {
        setUpRowsAndCols()
    }

    private fun setUpRowsAndCols() {
        formRows.clear()
        formColMaskList.clear()
        for (i in (0 until rows)) {
            val row = FormRow()
            for (j in (0 until cols)) {
                row.cells.add(FormCell(""))
            }
            formRows.add(row)
        }
        for (j in (0 until cols)) {
            formColMaskList.add(true)
        }
        initData()
    }

    fun getBodyString(): String {
        //Saving the rowXcol size
        val result = StringBuilder("SysMDForm")
        result.append(" $rows X $cols\n")

        //Adding to support Markdown rendering
        result.append("\n")

        //Saving the masks
        formColMaskList.forEachIndexed { index, it ->
            result.append(it.toString())
            if (index != formColMaskList.size - 1) {
                result.append(getFormColDelimiter())
            }
        }
        result.append(getFormRowDelimiter())

        //Adding to support Markdown rendering
        for (j in (0 until cols)) {
            result.append("|---")
        }

        result.append( getFormColDelimiter() + getFormRowDelimiter())

        //Saving Form Cells Content
        formRows.forEach { row ->
            row.cells.forEach { cell ->
                result.append(cell.value + getFormColDelimiter())
            }
            result.append(getFormRowDelimiter())
        }
        return result.toString()
    }

    fun updateRowsAndCols(r: Int, c: Int) {
        rows = r
        cols = c
        setUpRowsAndCols()
    }

    companion object Delimiters{
        fun getFormRowDelimiter(): String {
            return "\n"
        }

        fun getFormColDelimiter(): String {
            return "|"
        }
    }

}