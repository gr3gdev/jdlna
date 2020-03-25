package org.dev.gr3g.jdlna.utils

import org.dev.gr3g.jdlna.dao.DatabaseDAO
import org.dev.gr3g.jdlna.service.ContentDirectoryService
import org.dev.gr3g.jdlna.service.MSMediaReceiverRegistrarService
import org.dev.gr3g.jdlna.utils.FilesUtils.getFile
import org.fourthline.cling.binding.LocalServiceBindingException
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.ValidationException
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.DLNADoc
import org.fourthline.cling.model.types.DeviceType
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService
import org.fourthline.cling.support.model.ProtocolInfos
import org.fourthline.cling.support.model.dlna.DLNAProfiles
import org.fourthline.cling.support.model.dlna.DLNAProtocolInfo
import java.io.IOException
import java.net.URISyntaxException
import java.util.logging.Logger

/**
 * @author Gregory Tardivel
 */
object UpnpUtils {

    /** Logger.  */
    private val logger = Logger
            .getLogger(UpnpUtils::class.java.name)

    @Throws(ValidationException::class, LocalServiceBindingException::class, IOException::class, URISyntaxException::class)
    fun createDevice(): LocalDevice {
        val identifier = "JDLN-10111983"
        val manufacturer = "GR3Gdev"
        val modelName = DatabaseDAO.selectConf("name")
        val modelDescription = "Media server jDLNA"
        val modelNumber = "1.0.0"
        val identity = DeviceIdentity(
                UDN.uniqueSystemIdentifier(identifier))
        val type: DeviceType = UDADeviceType("MediaServer", 1)
        val manufDetails = ManufacturerDetails(
                manufacturer)
        val modelDetails = ModelDetails(modelName,
                modelDescription, modelNumber)
        val details = DeviceDetails(modelName, manufDetails,
                modelDetails, arrayOf(DLNADoc("DMS", DLNADoc.Version.V1_5)),
                null)
        val protocols = ProtocolInfos()
        for (dlnaProfile in DLNAProfiles.values()) {
            if (dlnaProfile == DLNAProfiles.NONE) {
                continue
            }
            try {
                protocols.add(DLNAProtocolInfo(dlnaProfile))
            } catch (exc: Exception) {
                // Silently ignored.
                logger.warning(exc.message)
            }
        }
        val directoryService: LocalService<ContentDirectoryService> = AnnotationLocalServiceBinder().read(ContentDirectoryService::class.java)
                as LocalService<ContentDirectoryService>
        directoryService.manager = DefaultServiceManager(directoryService, ContentDirectoryService::class.java)
        val connectionManagerService: LocalService<ConnectionManagerService> = AnnotationLocalServiceBinder().read(ConnectionManagerService::class.java)
                as LocalService<ConnectionManagerService>
        connectionManagerService.manager = object : DefaultServiceManager<ConnectionManagerService>(connectionManagerService) {
            @Throws(Exception::class)
            override fun createServiceInstance(): ConnectionManagerService {
                return ConnectionManagerService(protocols, null)
            }
        }

        // For compatibility with Microsoft
        val receiverService: LocalService<MSMediaReceiverRegistrarService> = AnnotationLocalServiceBinder().read(MSMediaReceiverRegistrarService::class.java)
                as LocalService<MSMediaReceiverRegistrarService>
        receiverService.manager = DefaultServiceManager(receiverService, MSMediaReceiverRegistrarService::class.java)
        val services: Array<LocalService<*>?> = arrayOfNulls(3)
        services[0] = connectionManagerService
        services[1] = directoryService
        services[2] = receiverService
        val icon300 = Icon("image/png", 300, 300, 32, "Icon300",
                getFile("/icones/logo-300.png"))
        val icon32 = Icon("image/png", 32, 32, 32, "Icon32",
                getFile("/icones/logo-32.png"))
        val icon48 = Icon("image/png", 48, 48, 32, "Icon48",
                getFile("/icones/logo-48.png"))
        return LocalDevice(identity, type, details, arrayOf(icon300, icon32, icon48), services)
    }
}